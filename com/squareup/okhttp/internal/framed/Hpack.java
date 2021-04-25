// 
// Decompiled by Procyon v0.5.36
// 

package com.squareup.okhttp.internal.framed;

import okio.Buffer;
import java.util.Collection;
import java.util.Arrays;
import okio.Okio;
import java.util.ArrayList;
import okio.Source;
import okio.BufferedSource;
import java.util.List;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import okio.ByteString;
import java.util.Map;

final class Hpack
{
    private static final int PREFIX_4_BITS = 15;
    private static final int PREFIX_5_BITS = 31;
    private static final int PREFIX_6_BITS = 63;
    private static final int PREFIX_7_BITS = 127;
    private static final Header[] STATIC_HEADER_TABLE;
    private static final Map<ByteString, Integer> NAME_TO_FIRST_INDEX;
    
    private Hpack() {
    }
    
    private static Map<ByteString, Integer> nameToFirstIndex() {
        final Map<ByteString, Integer> result = new LinkedHashMap<ByteString, Integer>(Hpack.STATIC_HEADER_TABLE.length);
        for (int i = 0; i < Hpack.STATIC_HEADER_TABLE.length; ++i) {
            if (!result.containsKey(Hpack.STATIC_HEADER_TABLE[i].name)) {
                result.put(Hpack.STATIC_HEADER_TABLE[i].name, i);
            }
        }
        return Collections.unmodifiableMap((Map<? extends ByteString, ? extends Integer>)result);
    }
    
    private static ByteString checkLowercase(final ByteString name) throws IOException {
        for (int i = 0, length = name.size(); i < length; ++i) {
            final byte c = name.getByte(i);
            if (c >= 65 && c <= 90) {
                throw new IOException("PROTOCOL_ERROR response malformed: mixed case name: " + name.utf8());
            }
        }
        return name;
    }
    
    static {
        STATIC_HEADER_TABLE = new Header[] { new Header(Header.TARGET_AUTHORITY, ""), new Header(Header.TARGET_METHOD, "GET"), new Header(Header.TARGET_METHOD, "POST"), new Header(Header.TARGET_PATH, "/"), new Header(Header.TARGET_PATH, "/index.html"), new Header(Header.TARGET_SCHEME, "http"), new Header(Header.TARGET_SCHEME, "https"), new Header(Header.RESPONSE_STATUS, "200"), new Header(Header.RESPONSE_STATUS, "204"), new Header(Header.RESPONSE_STATUS, "206"), new Header(Header.RESPONSE_STATUS, "304"), new Header(Header.RESPONSE_STATUS, "400"), new Header(Header.RESPONSE_STATUS, "404"), new Header(Header.RESPONSE_STATUS, "500"), new Header("accept-charset", ""), new Header("accept-encoding", "gzip, deflate"), new Header("accept-language", ""), new Header("accept-ranges", ""), new Header("accept", ""), new Header("access-control-allow-origin", ""), new Header("age", ""), new Header("allow", ""), new Header("authorization", ""), new Header("cache-control", ""), new Header("content-disposition", ""), new Header("content-encoding", ""), new Header("content-language", ""), new Header("content-length", ""), new Header("content-location", ""), new Header("content-range", ""), new Header("content-type", ""), new Header("cookie", ""), new Header("date", ""), new Header("etag", ""), new Header("expect", ""), new Header("expires", ""), new Header("from", ""), new Header("host", ""), new Header("if-match", ""), new Header("if-modified-since", ""), new Header("if-none-match", ""), new Header("if-range", ""), new Header("if-unmodified-since", ""), new Header("last-modified", ""), new Header("link", ""), new Header("location", ""), new Header("max-forwards", ""), new Header("proxy-authenticate", ""), new Header("proxy-authorization", ""), new Header("range", ""), new Header("referer", ""), new Header("refresh", ""), new Header("retry-after", ""), new Header("server", ""), new Header("set-cookie", ""), new Header("strict-transport-security", ""), new Header("transfer-encoding", ""), new Header("user-agent", ""), new Header("vary", ""), new Header("via", ""), new Header("www-authenticate", "") };
        NAME_TO_FIRST_INDEX = nameToFirstIndex();
    }
    
    static final class Reader
    {
        private final List<Header> headerList;
        private final BufferedSource source;
        private int headerTableSizeSetting;
        private int maxDynamicTableByteCount;
        Header[] dynamicTable;
        int nextHeaderIndex;
        int headerCount;
        int dynamicTableByteCount;
        
        Reader(final int headerTableSizeSetting, final Source source) {
            this.headerList = new ArrayList<Header>();
            this.dynamicTable = new Header[8];
            this.nextHeaderIndex = this.dynamicTable.length - 1;
            this.headerCount = 0;
            this.dynamicTableByteCount = 0;
            this.headerTableSizeSetting = headerTableSizeSetting;
            this.maxDynamicTableByteCount = headerTableSizeSetting;
            this.source = Okio.buffer(source);
        }
        
        int maxDynamicTableByteCount() {
            return this.maxDynamicTableByteCount;
        }
        
        void headerTableSizeSetting(final int headerTableSizeSetting) {
            this.headerTableSizeSetting = headerTableSizeSetting;
            this.maxDynamicTableByteCount = headerTableSizeSetting;
            this.adjustDynamicTableByteCount();
        }
        
        private void adjustDynamicTableByteCount() {
            if (this.maxDynamicTableByteCount < this.dynamicTableByteCount) {
                if (this.maxDynamicTableByteCount == 0) {
                    this.clearDynamicTable();
                }
                else {
                    this.evictToRecoverBytes(this.dynamicTableByteCount - this.maxDynamicTableByteCount);
                }
            }
        }
        
        private void clearDynamicTable() {
            this.headerList.clear();
            Arrays.fill(this.dynamicTable, null);
            this.nextHeaderIndex = this.dynamicTable.length - 1;
            this.headerCount = 0;
            this.dynamicTableByteCount = 0;
        }
        
        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                for (int j = this.dynamicTable.length - 1; j >= this.nextHeaderIndex && bytesToRecover > 0; bytesToRecover -= this.dynamicTable[j].hpackSize, this.dynamicTableByteCount -= this.dynamicTable[j].hpackSize, --this.headerCount, ++entriesToEvict, --j) {}
                System.arraycopy(this.dynamicTable, this.nextHeaderIndex + 1, this.dynamicTable, this.nextHeaderIndex + 1 + entriesToEvict, this.headerCount);
                this.nextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }
        
        void readHeaders() throws IOException {
            while (!this.source.exhausted()) {
                final int b = this.source.readByte() & 0xFF;
                if (b == 128) {
                    throw new IOException("index == 0");
                }
                if ((b & 0x80) == 0x80) {
                    final int index = this.readInt(b, 127);
                    this.readIndexedHeader(index - 1);
                }
                else if (b == 64) {
                    this.readLiteralHeaderWithIncrementalIndexingNewName();
                }
                else if ((b & 0x40) == 0x40) {
                    final int index = this.readInt(b, 63);
                    this.readLiteralHeaderWithIncrementalIndexingIndexedName(index - 1);
                }
                else if ((b & 0x20) == 0x20) {
                    this.maxDynamicTableByteCount = this.readInt(b, 31);
                    if (this.maxDynamicTableByteCount < 0 || this.maxDynamicTableByteCount > this.headerTableSizeSetting) {
                        throw new IOException("Invalid dynamic table size update " + this.maxDynamicTableByteCount);
                    }
                    this.adjustDynamicTableByteCount();
                }
                else if (b == 16 || b == 0) {
                    this.readLiteralHeaderWithoutIndexingNewName();
                }
                else {
                    final int index = this.readInt(b, 15);
                    this.readLiteralHeaderWithoutIndexingIndexedName(index - 1);
                }
            }
        }
        
        public List<Header> getAndResetHeaderList() {
            final List<Header> result = new ArrayList<Header>(this.headerList);
            this.headerList.clear();
            return result;
        }
        
        private void readIndexedHeader(final int index) throws IOException {
            if (this.isStaticHeader(index)) {
                final Header staticEntry = Hpack.STATIC_HEADER_TABLE[index];
                this.headerList.add(staticEntry);
            }
            else {
                final int dynamicTableIndex = this.dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex > this.dynamicTable.length - 1) {
                    throw new IOException("Header index too large " + (index + 1));
                }
                this.headerList.add(this.dynamicTable[dynamicTableIndex]);
            }
        }
        
        private int dynamicTableIndex(final int index) {
            return this.nextHeaderIndex + 1 + index;
        }
        
        private void readLiteralHeaderWithoutIndexingIndexedName(final int index) throws IOException {
            final ByteString name = this.getName(index);
            final ByteString value = this.readByteString();
            this.headerList.add(new Header(name, value));
        }
        
        private void readLiteralHeaderWithoutIndexingNewName() throws IOException {
            final ByteString name = checkLowercase(this.readByteString());
            final ByteString value = this.readByteString();
            this.headerList.add(new Header(name, value));
        }
        
        private void readLiteralHeaderWithIncrementalIndexingIndexedName(final int nameIndex) throws IOException {
            final ByteString name = this.getName(nameIndex);
            final ByteString value = this.readByteString();
            this.insertIntoDynamicTable(-1, new Header(name, value));
        }
        
        private void readLiteralHeaderWithIncrementalIndexingNewName() throws IOException {
            final ByteString name = checkLowercase(this.readByteString());
            final ByteString value = this.readByteString();
            this.insertIntoDynamicTable(-1, new Header(name, value));
        }
        
        private ByteString getName(final int index) {
            if (this.isStaticHeader(index)) {
                return Hpack.STATIC_HEADER_TABLE[index].name;
            }
            return this.dynamicTable[this.dynamicTableIndex(index - Hpack.STATIC_HEADER_TABLE.length)].name;
        }
        
        private boolean isStaticHeader(final int index) {
            return index >= 0 && index <= Hpack.STATIC_HEADER_TABLE.length - 1;
        }
        
        private void insertIntoDynamicTable(int index, final Header entry) {
            this.headerList.add(entry);
            int delta = entry.hpackSize;
            if (index != -1) {
                delta -= this.dynamicTable[this.dynamicTableIndex(index)].hpackSize;
            }
            if (delta > this.maxDynamicTableByteCount) {
                this.clearDynamicTable();
                return;
            }
            final int bytesToRecover = this.dynamicTableByteCount + delta - this.maxDynamicTableByteCount;
            final int entriesEvicted = this.evictToRecoverBytes(bytesToRecover);
            if (index == -1) {
                if (this.headerCount + 1 > this.dynamicTable.length) {
                    final Header[] doubled = new Header[this.dynamicTable.length * 2];
                    System.arraycopy(this.dynamicTable, 0, doubled, this.dynamicTable.length, this.dynamicTable.length);
                    this.nextHeaderIndex = this.dynamicTable.length - 1;
                    this.dynamicTable = doubled;
                }
                index = this.nextHeaderIndex--;
                this.dynamicTable[index] = entry;
                ++this.headerCount;
            }
            else {
                index += this.dynamicTableIndex(index) + entriesEvicted;
                this.dynamicTable[index] = entry;
            }
            this.dynamicTableByteCount += delta;
        }
        
        private int readByte() throws IOException {
            return this.source.readByte() & 0xFF;
        }
        
        int readInt(final int firstByte, final int prefixMask) throws IOException {
            final int prefix = firstByte & prefixMask;
            if (prefix < prefixMask) {
                return prefix;
            }
            int result = prefixMask;
            int shift = 0;
            int b;
            while (true) {
                b = this.readByte();
                if ((b & 0x80) == 0x0) {
                    break;
                }
                result += (b & 0x7F) << shift;
                shift += 7;
            }
            result += b << shift;
            return result;
        }
        
        ByteString readByteString() throws IOException {
            final int firstByte = this.readByte();
            final boolean huffmanDecode = (firstByte & 0x80) == 0x80;
            final int length = this.readInt(firstByte, 127);
            if (huffmanDecode) {
                return ByteString.of(Huffman.get().decode(this.source.readByteArray(length)));
            }
            return this.source.readByteString(length);
        }
    }
    
    static final class Writer
    {
        private final Buffer out;
        
        Writer(final Buffer out) {
            this.out = out;
        }
        
        void writeHeaders(final List<Header> headerBlock) throws IOException {
            for (int i = 0, size = headerBlock.size(); i < size; ++i) {
                final ByteString name = headerBlock.get(i).name.toAsciiLowercase();
                final Integer staticIndex = Hpack.NAME_TO_FIRST_INDEX.get(name);
                if (staticIndex != null) {
                    this.writeInt(staticIndex + 1, 15, 0);
                    this.writeByteString(headerBlock.get(i).value);
                }
                else {
                    this.out.writeByte(0);
                    this.writeByteString(name);
                    this.writeByteString(headerBlock.get(i).value);
                }
            }
        }
        
        void writeInt(int value, final int prefixMask, final int bits) throws IOException {
            if (value < prefixMask) {
                this.out.writeByte(bits | value);
                return;
            }
            this.out.writeByte(bits | prefixMask);
            int b;
            for (value -= prefixMask; value >= 128; value >>>= 7) {
                b = (value & 0x7F);
                this.out.writeByte(b | 0x80);
            }
            this.out.writeByte(value);
        }
        
        void writeByteString(final ByteString data) throws IOException {
            this.writeInt(data.size(), 127, 0);
            this.out.write(data);
        }
    }
}
