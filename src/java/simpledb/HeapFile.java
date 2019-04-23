package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File f;
    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
//        throw new UnsupportedOperationException("implement this");
        return f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
//        throw new UnsupportedOperationException("implement this");
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        try {
            Page new_page;
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            byte[] data = new byte[BufferPool.getPageSize()];
            raf.seek(pid.pageNumber() * BufferPool.getPageSize());
            raf.read(data, 0, BufferPool.getPageSize());
            raf.close();
            HeapPage page = new HeapPage((HeapPageId) pid, data);
            return page;
        } catch(IOException e) {
            // ignore
        }
        return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)Math.ceil(((double)f.length())/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(this, tid);
    }

    class HeapFileIterator implements DbFileIterator{

        private HeapFile heap_file;
        private TransactionId tid;
        private Iterator<Tuple> tuple_in_page;
        private HeapPage page;
        private int page_num;
        private boolean is_open;

        public HeapFileIterator(HeapFile heap_file, TransactionId tid) {
            this.heap_file = heap_file;
            this.tid = tid;
        }

        public void open() throws DbException, TransactionAbortedException{
            if (is_open) throw new DbException("This iterator is already opened!");
            is_open = true;
            this.rewind();
        }

        private int findNext() throws DbException, TransactionAbortedException, NoSuchElementException{
            if (tuple_in_page == null) return -1;
            if (!is_open) return -1;
            if (tuple_in_page.hasNext()) return 0;
            int tmp_page = page_num;
            Iterator<Tuple> t;
            while (tmp_page < numPages()-1){
                tmp_page++;
//                System.out.println(String.valueOf(tmp_page)+"find");
//                System.out.println(new HeapPageId(getId(), tmp_page).hashCode()+"hash");
                t = ((HeapPage) Database.getBufferPool().getPage(tid, new HeapPageId(getId(), tmp_page), Permissions.READ_WRITE)).iterator();
                if (t.hasNext()) return tmp_page;
            }
            return -1;
        }


        public boolean hasNext() throws DbException, TransactionAbortedException{
            if (findNext() == -1) return false;
            else return true;
        }

        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException{
//            if (!is_open) throw new DbException("This iterator is not open!");
            int flag = findNext();
            if (flag == -1) throw new NoSuchElementException("This iterator is not open!");
            if (flag == 0) {
                return tuple_in_page.next();
            }
            else {
                page_num = flag;
//                System.out.println(String.valueOf(page_num)+"next");
//                System.out.println(new HeapPageId(getId(), page_num).hashCode()+"hash");
                page = (HeapPage) Database.getBufferPool().getPage(tid, new HeapPageId(getId(), page_num), Permissions.READ_WRITE);
                tuple_in_page = page.iterator();
                return tuple_in_page.next();
            }
        }

        public void rewind() throws DbException, TransactionAbortedException{
            if (is_open == false) throw new DbException("This iterator is already opened!");
            page_num = 0;
//            System.out.println("0"+" rewind");
//            System.out.println(new HeapPageId(getId(), 0).hashCode() +"hash");
            this.page = (HeapPage) Database.getBufferPool().getPage(tid, new HeapPageId(getId(), 0), Permissions.READ_WRITE);
            tuple_in_page = page.iterator();
        }

        public void close() {
            is_open = false;
        }

    }

}




