package com.treeblossom.util.bincompare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * 
 * Looks for a byte sequence in a file; if the sequence occurs in the file, a
 * new file is created based on the file but minus the sequence. If the sequence
 * does not occur in the file, then no new file is generated.
 * @author Jonathan Donald
 * 
 *
 */
public class SubtractSequence {

    /**
     * Represents a finite stream of bytes found in a file, with a defined
     * starting and ending point.
     */
    public class Chunk {
        
        /**
         * The position of the first byte in the <tt>Chunk</tt>.   
         */
        long startPos = 0L;
        /**
         * The position of the last byte in the <tt>Chunk</tt>.
         */
        long endPos = 0L;

        /**
         * the overall length of the <tt>Chunk</tt> in bytes.
         * @return
         */
        long getLength() {
            return endPos - startPos;
        }
        String filePath = null;
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        SubtractSequence bc = new SubtractSequence();
        Calendar startTime = Calendar.getInstance();
        System.out.printf("started at %2d:%2d:%2d\n", startTime.get(Calendar.HOUR),
                startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND));
        bc.doSubtract(args);
        Calendar endTime = Calendar.getInstance();
        System.out.printf("finished at %2d:%2d:%2d\n", endTime.get(Calendar.HOUR),
                endTime.get(Calendar.MINUTE), endTime.get(Calendar.SECOND));
        System.out.printf("elapsed time: %d seconds \n",
                Math.round((endTime.getTimeInMillis() - startTime.getTimeInMillis())
                        / 1000.0));
    }

    /**
     * @param args
     */
    void doSubtract(String[] args) throws IOException {
        SubtractSequence.Chunk chunkA = new Chunk();
        SubtractSequence.Chunk chunkB = new Chunk();
        chunkA.filePath = args[0];
        chunkB.filePath = args[1];
        chunkA.startPos = 32;
        chunkA.endPos = new File(chunkA.filePath).length() - 1;
        chunkB.startPos = 0;
        chunkB.endPos = new File(chunkB.filePath).length() - 1;
        long foundPos = -1;
        try {
            foundPos = findIn(chunkA, chunkB);
        } catch (IOException ioe) {
            System.out.printf("I/O error: " + ioe.getMessage());
        }
        System.out.println();
        if (foundPos > -1) {
            System.out.printf("File %s occurs in %s starting at position %d\n",
                    chunkA.filePath, chunkB.filePath, foundPos);
            chunkB.startPos = foundPos;
            subtract(chunkB, foundPos, foundPos + chunkA.getLength());
        } else {
            System.out.printf("File %s is not found in file %s",
                    chunkA.filePath, chunkB.filePath);
        }
    }

    /**
     * Creates a new file based on the <tt>chunkB</tt> file, but minus the sequence starting at byte <tt>start</tt> and ending at byte <tt>end</tt>.
     * 
     * 
     *
     */
    void subtract(Chunk chunkB, long start, long end) throws IOException {

        long bPosition = 0L;

        FileInputStream inputStreamB = null;
        FileOutputStream outputStreamNew = null;
        String newFilePath = chunkB.filePath + ".new";
        try {
            inputStreamB = new FileInputStream(chunkB.filePath);
            outputStreamNew = new FileOutputStream(newFilePath);
            int byteB;
            boolean moreBytesB = true;
            System.out.printf("creating new file %s\n", newFilePath);

            // write out everything up to the chunkB.startPos to the new file
            while (moreBytesB && bPosition < start) {
                moreBytesB = ((byteB = inputStreamB.read()) != -1);
                bPosition++;
                if (!moreBytesB) {
                    break;
                }
                outputStreamNew.write(byteB);
            }
            // skip past the undesired chunk
            System.out.printf("fast-forwarding from position %d to %d\n", start,
                    end);
            fastForward(inputStreamB, end - start);

            System.out.printf("writing out from position %d to end\n",
                    end);
            // write out everything from that point to the end of chunkB
            while (moreBytesB) {
                moreBytesB = ((byteB = inputStreamB.read()) != -1);
                if (!moreBytesB) {
                    break;
                }
                outputStreamNew.write(byteB);
            }
            // close the files
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStreamB != null)
                    inputStreamB.close();
            } catch (IOException ioe1) {
                System.err.printf("problem closing %s", chunkB.filePath);
            }
            try {
                if (outputStreamNew != null)
                    outputStreamNew.close();
            } catch (IOException ioe1) {
                System.err.printf("problem closing %s", newFilePath);
            }
        }
    }
    
    /**
     * Returns position of file A in file B.
     * 
     * @param chunkA
     * @param chunkB
     * @return
     */
    long findIn(Chunk chunkA, Chunk chunkB) throws IOException {

        long aPosition = chunkA.startPos;
        long bPosition = chunkB.startPos;
        long foundPos = -1;
        boolean result = false;

        while (result == false) {
            foundPos = scan(chunkA, chunkB);
            if (foundPos > -1) {
                result = true;
            } else {
                result = false;
                // System.out.printf("It's not at position %d\n",
                // chunkB.startPos);//DEBUG
                System.out.println(
                        "-----------------------------------------------------------------------------------");
                chunkB.startPos++;
                if (chunkB.startPos >= chunkB.endPos) {
                    break;
                }
            }
        } // end while
        return foundPos;
    }

    /**
     * Scan the file pointed to by <tt>chunkB</tt> to see if it contains the sequence of
     * bytes pointed to by <tt>chunkA</tt>. If found, it returns the position in the 'B'
     * file where the 'A' sequence begins.
     * 
     * @param chunkA
     * @param chunkB
     * @return
     */
    long scan(Chunk chunkA, Chunk chunkB) throws IOException {

        long position = chunkB.startPos;
        boolean result = true;
        FileInputStream inputStreamA = null;
        FileInputStream inputStreamB = null;
                
        try {
            inputStreamA = new FileInputStream(chunkA.filePath);
            inputStreamB = new FileInputStream(chunkB.filePath);
            int byteA, byteB;
            boolean moreBytesA = true, moreBytesB = true;

            fastForward(inputStreamA, chunkA.startPos);
            fastForward(inputStreamB, chunkB.startPos);
            while (moreBytesA && moreBytesB) {
                moreBytesA = ((byteA = inputStreamA.read()) != -1);
                moreBytesB = ((byteB = inputStreamB.read()) != -1);
                if (!moreBytesA) {
                    break;
                }
                // System.out.printf("\t%d %c : %d
                // %c\n",byteA,byteA,byteB,byteB);//DEBUG
                position++;
                if (byteA != byteB) {
                    System.out.print("-");
                    result = false;
                    break;
                }
                System.out.print("+");
            }
            // System.out.println("reached end of file " + (moreBytesA ? " A" :
            // moreBytesB ? " B" : ""));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStreamA != null)
                    inputStreamA.close();
            } catch (IOException ioe1) {
            }
            try {
                if (inputStreamB != null)
                    inputStreamB.close();
            } catch (IOException ioe1) {
            }
        }
        return (result == true) ? position : -1;
    }

    /**
     * Moves the <tt>read()</tt> pointer in <tt>fis</tt> by <tt>numBytes</tt>.
     * 
     * @param fis
     * @param numBytes
     * @throws IOException
     */
    void fastForward(FileInputStream fis, long numBytes) throws IOException {
        if (fis == null) {
            throw new IllegalArgumentException("null file specified");
        }
        if (numBytes < 0) {
            throw new IllegalArgumentException(
                    "Cannot fast forward a negative number of bytes");
        }
        byte[] b = new byte[(int) numBytes];
        fis.read(b);
    }

}
