/*
 * Copyright (c) 2021 Nico Kuijpers and Marco Brassé
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR I
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * This code was converted from Python3 reader by Marco Brassé
 * Original Python2 code from
 * (C) 2017 Nabla Zero Labs <Juan.Arrieta@nablazerolabs.com>
 */
package ephemeris;

import util.Vector3D;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SPK {

    // Auxiliary class
    public class SummaryRecords
    {
        private int nRecords;
        public double[] _etbeg;
        public double[] _etend;
        public int[] _t;
        public int[] _o;
        public int[] _frame;
        public int[] _type;
        public int[] _rbeg;
        public int[] _rend;
        public SummaryRecords()
        {
            nRecords=0;
            _etbeg = new double[128];
            _etend = new double[128];
            _t = new int[128];
            _o = new int[128];
            _frame = new int[128];
            _type = new int[128];
            _rbeg = new int[128];
            _rend = new int[128];
        }
        public void addRecord( double etbeg, double etend, int t, int o, int frame, int type, int rbeg, int rend)
        {
            if (nRecords<128)
            {
                _etbeg[nRecords] = etbeg;
                _etend[nRecords] = etend;
                _t[nRecords]= t;
                _o[nRecords] = o;
                _frame[nRecords] = frame;
                _type[nRecords] = type;
                _rbeg[nRecords] = rbeg;
                _rend[nRecords] = rend;
                nRecords = nRecords + 1;
            }
        }
        public int searchRecord(double et, int target, int observer)
        {
            int res = -1;
            int m = 0;
            while ( m != nRecords )
            {
                if (((_t[m]==target) && (_o[m]==observer)) && ((_etbeg[m] <= et) && (et <= _etend[m])))
                {
                    res = m;
                    m = nRecords;
                }
                else
                {
                    m = m + 1;
                }
            }
            return res;
        }
    };

    private final int RECLEN = 1024;
    private double[] data;
    private byte[] bbuf;
    private SummaryRecords summary;
    private MappedByteBuffer buffer;
    private Boolean isInit;
    private Boolean isLittleEndian;

    public SPK() {
        data = new double[128]; // DAF spec
        summary = new SummaryRecords();
        isInit = false;
        isLittleEndian = Boolean.TRUE;
    }

    private Boolean isEqualByteArray(byte[] a, byte[] b, int n) {
        return ByteBuffer.wrap(a, 0, n).equals(ByteBuffer.wrap(b, 0, n));
    }

    private long getUnsignedInt(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);

        if (isLittleEndian == Boolean.TRUE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        return bb.getInt() & 0xffffffffl;
    }

    private double getDouble(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);

        if (isLittleEndian == Boolean.TRUE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        return bb.getDouble();
    }

    private double chebyshev(int order, double x, int offset) {
        double two_x = 2 * x;
        double bkp2 = data[offset+order];
        double bkp1 = two_x * bkp2 + data[offset+order - 1];
        for (int n=order-2; n>0; n=n-1) {
            double bk = data[offset+n] + two_x * bkp1 - bkp2;
            bkp2 = bkp1;
            bkp1 = bk;
        }
        return data[offset] + x * bkp1 - bkp2;
    }

    private double der_chebyshev(int order, double x, int offset) {
        double two_x = 2 * x;
        double bkp2 = order * data[offset+order];
        double bkp1 = two_x * bkp2 + (order - 1) * data[offset+order - 1];
        for (int n = order - 2; n > 1; n = n - 1) {
            double bk = n * data[offset+n] + two_x * bkp1 - bkp2;
            bkp2 = bkp1;
            bkp1 = bk;
        }
        return data[offset+1] + two_x * bkp1 - bkp2;
    }

    /**
     * Initialize with BSP file
     * @param path filepath to .bsp file
     */
    public void initWithBSPFile(String path) {
        String DAFstr  = "DAF/SPK";
        String NAIFstr = "NAIF/DA";
        String LEstr = "LTL-IEEE";

        bbuf = new byte[1024];
        int nd;
        int ni;
        int bward;
        int fward;

        try (RandomAccessFile file = new RandomAccessFile(new File(path), "r"))
        {
            if (path.indexOf("405")>=0)
            {
                isLittleEndian = Boolean.FALSE;
            }
            else {
                isLittleEndian = Boolean.TRUE;
            }
            //Get file channel in read-only mode
            FileChannel fileChannel = file.getChannel();

            //Get direct byte buffer access using channel.map() operation
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            //You can read the file from this buffer the way you like.
            buffer.get(bbuf,0,7);
            if ((!isEqualByteArray(bbuf, DAFstr.getBytes(), 7)) && ((!isEqualByteArray(bbuf, NAIFstr.getBytes(), 7)))) {
                System.err.println("ERROR: Wrong BSP File " + path);
                return;
            }
            //buffer.position(88);
            //buffer.get(bbuf,0,8);
            //if (isEqualByteArray(bbuf, LEstr.getBytes(), 8))
            //{
            //System.out.println("[DEBUG] Little endianness");
            //}
            buffer.position(8);
            buffer.get(bbuf, 0, 4);
            nd = (int) getUnsignedInt(bbuf);
            //
            buffer.position(12);
            buffer.get(bbuf, 0, 4);
            ni = (int) getUnsignedInt(bbuf);
            //
            buffer.position(76);
            buffer.get(bbuf, 0, 4);
            fward = (int) getUnsignedInt(bbuf);
            //
            buffer.position(80);
            buffer.get(bbuf, 0, 4);
            bward = (int) getUnsignedInt(bbuf);
            //
            assert(nd==2);
            assert(ni==6);
            //
            int summary_offset = (fward - 1) * RECLEN;
            int summary_size = nd + (ni + 1) / 2; // integer division
            int nxt=1;
            int prv=0;
            int nsum=0;
            while (nxt!=0)
            {
                int n;
                buffer.position(summary_offset);
                buffer.get(bbuf, 0, 8);
                nxt = (int) ((double) getDouble(bbuf));
                buffer.position(summary_offset+8);
                buffer.get(bbuf, 0, 8);
                prv = (int) ((double) getDouble(bbuf)); // not used
                buffer.position(summary_offset+16);
                buffer.get(bbuf, 0, 8);
                nsum = (int) ((double) getDouble(bbuf));
                summary_offset = summary_offset + 24;
                for (int i = 0; i<nsum; i++)
                {
                    double[] drec = new double[nd];
                    int[] irec = new int[ni];
                    // read nd==2 doubles
                    buffer.position(summary_offset);
                    for (int j = 0; j<(int) nd; j++)
                    {
                        double rec;
                        buffer.get(bbuf, 0, 8);
                        drec[j] = (double) getDouble(bbuf);
                        //System.out.println("...read double " + drec[j]);
                    }
                    // read ni==6 unsigned int
                    for (int j = 0; j<(int) ni; j++)
                    {
                        buffer.get(bbuf, 0, 4);
                        irec[j] = (int) getUnsignedInt(bbuf);
                        //System.out.println("...read uint " + irec[j]);
                    }
                    summary_offset = summary_offset + summary_size * 8; // bytes
                    summary.addRecord(drec[0],drec[1],irec[0],irec[1],irec[2],irec[3],irec[4],irec[5]);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("ERROR: Error opening BSP file" + path);
        }
        isInit = true;
        return;
    }

    /**
     * Compute position and velocity for target object relative to observer.
     * @param et         Number of seconds since J2000 (can be negative)
     * @param target     Code for target object
     * @param observer   Code for server object
     * @return position [m] and velocity [m/s]
     */
    public Vector3D[] getPositionVelocity(double et, int target, int observer) {
        // et is the relative time in seconds from J2000, so can be minus or plus
        // target is an index, i.e. 0 for SBB, 3 for EMB, 901 for Charon, etc
        // observer is an index (same as target)
        Vector3D[] result =  new Vector3D[2];
        result[0] = new Vector3D();
        result[1] = new Vector3D();
        int order;
        if (!isInit)
        {
            System.out.println("SPK::getPosVel, class instance not initialized");
            return result;
        }
        int idx = summary.searchRecord(et,target,observer);
        if (idx==-1)
        {
            System.out.println("SPK::getPosVel, Cannot find record");
            System.out.println(" et = " + et);
            System.out.println(" target = " + target);
            System.out.println(" observer = " + observer);
            return result;
        }
        int type = summary._type[idx];
        int rbeg = summary._rbeg[idx];
        int rend = summary._rend[idx];
        int offset = (rend - 4) * 8;
        //read four doubles according to DAF spec
        buffer.position(offset);
        buffer.get(bbuf, 0, 8);
        double init = (double) getDouble(bbuf);
        buffer.get(bbuf, 0, 8);
        double intlen = (double) getDouble(bbuf);
        buffer.get(bbuf, 0, 8);
        double rsize = (double) getDouble(bbuf);
        buffer.get(bbuf, 0, 8);
        double _n = (double) getDouble(bbuf);
        int internal_offset = (int) (Math.floor((et - init) / intlen) * rsize);
        int record = 8 * (int)(rbeg + internal_offset);
        double px=0.0,py=0.0,pz=0.0;
        double vx=0.0,vy=0.0,vz=0.0;

        if (type == 2) // Type II interpolation
        {
            for (int i=0; i<(int)rsize; i++)
            {
                buffer.position(((record-8)+i*8));
                buffer.get(bbuf, 0, 8);
                data[i] = (double) getDouble(bbuf);
            }
            order = (int) (((int)(rsize) - 2) / 3 - 1);
            double tau = (et - data[0]) / data[1];
            int deg = (int)(order  + 1);
            double  factor = 1.0 / data[1];

            px = chebyshev(order, tau, 2+0*deg);
            py = chebyshev(order, tau, 2+1*deg);
            pz = chebyshev(order, tau, 2+2*deg);
            vx = der_chebyshev(order, tau, 2+0*deg)*factor;
            vy = der_chebyshev(order, tau, 2+1*deg)*factor;
            vz = der_chebyshev(order, tau, 2+2*deg)*factor;
        }
        else if (type == 3) // Type III interpolation
        {
            for (int i=0; i<(int)rsize; i++)
            {
                buffer.position((record-8)+i*8);
                buffer.get(bbuf, 0, 8);
                data[i] = (double) getDouble(bbuf);
            }
            order =(int)(((int)(rsize) - 2) / 6 - 1);
            double tau = (et - data[0]) / data[1];
            int deg = (int)(order  + 1);
            px = chebyshev(order, tau, 2+0*deg);
            py = chebyshev(order, tau, 2+1*deg);
            pz = chebyshev(order, tau, 2+2*deg);
            vx = chebyshev(order, tau, 2+3*deg);
            vy = chebyshev(order, tau, 2+4*deg);
            vz = chebyshev(order, tau, 2+5*deg);
        }
        else
        {
            System.out.println("Type not recognized");
            return result;
        }

        // Convert position to m and velocity to m/s
        result[0] = new Vector3D(px*1000,py*1000,pz*1000);
        result[1] = new Vector3D(vx*1000,vy*1000,vz*1000);
        return result;
    }
}