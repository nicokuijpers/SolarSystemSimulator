package ephemeris;

/*
 * Copyright (c) 2022 Nico Kuijpers and Marco Brassé
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
 * Based on Python2 code from
 * 2017 Nabla Zero Labs <Juan.Arrieta@nablazerolabs.com>
 */

import util.Vector3D;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class SPK {

    final int MAX_RECORDS = 1024;

    // AUXILIARY CLASS
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
            _etbeg = new double[MAX_RECORDS];
            _etend = new double[MAX_RECORDS];
            _t = new int[MAX_RECORDS];
            _o = new int[MAX_RECORDS];
            _frame = new int[MAX_RECORDS];
            _type = new int[MAX_RECORDS];
            _rbeg = new int[MAX_RECORDS];
            _rend = new int[MAX_RECORDS];
        }
        public void addRecord( double etbeg, double etend, int t, int o, int frame, int type, int rbeg, int rend)
        {
            if (nRecords<MAX_RECORDS)
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
                    m=m+1;
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

    private Boolean isEqualByteArray(byte[] a, byte[] b, int n)
    {
        return ByteBuffer.wrap(a, 0, n).equals(ByteBuffer.wrap(b, 0, n));
    }

    private long getUnsignedInt(byte[] data)
    {
        ByteBuffer bb = ByteBuffer.wrap(data);

        if (isLittleEndian == Boolean.TRUE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        return bb.getInt() & 0xffffffffl;
    }

    private double getDouble(byte[] data)
    {
        ByteBuffer bb = ByteBuffer.wrap(data);

        if (isLittleEndian == Boolean.TRUE) {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }
        else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        return bb.getDouble();
    }

    private double chebyshev(int order, double x, int offset)
    {
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

    private double[] spke01(double ET, double[] RECORD)
    {
        //     Based on original FORTRAN code by Fred T. Krogh
        //     Unpack the contents of the MDA array.
        //
        //        Name    Dimension  Description
        //       ------  ---------  -------------------------------
        //       TL              1  Final epoch of record
        //       G              15  Step size function vector
        //       REFPOS          3  Reference position vector
        //       REFVEL          3  Reference velocity vector
        //       DT         15,NTE  Modified divided difference arrays
        //       KQMAX1          1  Maximum integration order plus 1
        //       KQ            NTE  Integration order array
        //
        //        For our purposes, NTE is always 3.
        //
        double TL = RECORD[0];
        double[] G = new double[15];
        for (int i=0; i<15; i++) G[i] = RECORD[i+1];
        double[] REFPOS = new double[3];
        double[] REFVEL = new double[3];
        REFPOS[0] = RECORD[16];
        REFPOS[1] = RECORD[18];
        REFPOS[2] = RECORD[20];
        REFVEL[0] = RECORD[17];
        REFVEL[1] = RECORD[19];
        REFVEL[2] = RECORD[21];
        double[] DT = new double[45];
        for (int i=0; i<45; i++) DT[i] = RECORD[i+22];
        int KQMAX1 = (int)(RECORD[67]);
        int[] KQ = new int[3];
        KQ[0] = (int)(RECORD[68]);
        KQ[1] = (int)(RECORD[69]);
        KQ[2] = (int)(RECORD[70]);
        double DELTA = ET - TL;
        double TP = DELTA;
        int MQ2 = KQMAX1 - 2;
        int KS = KQMAX1 - 1;
        double[] FC = new double [15];
        for (int i=0; i<15; i++) FC[i] = 0.0;
        FC[0] = 1.0;
        double[] WC = new double[13];
        for (int i=0; i<13; i++) WC[i] = 0.0;
        double[] W=new double[17];
        for (int i=0; i<17; i++) W[i] = 0.0;

        for (int J=1; J<MQ2 + 1; J++)
        {
            FC[J] = TP / G[J-1];
            WC[J-1] = DELTA / G[J-1];
            TP = DELTA +G[J-1];
        }
        for (int J=1; J<KQMAX1 + 1; J++)
        {
            W[J-1] = 1.0 / (double)(J);
        }

        int JX = 0;
        int KS1 = KS - 1;
        while (KS>=2)
        {
            JX = JX + 1;
            for (int J=1; J<JX + 1; J++)
            {
                W[J+KS-1] = FC[J] * W[J+KS1-1] - WC[J-1] * W[J+KS-1];
            }
            KS = KS1;
            KS1 = KS1 - 1;
        }

        int KQQ;
        double SUM = 0.0;
        double[] STATE=new double[6];

        for (int I=1; I<3 + 1; I++)
        {
            KQQ = KQ[I-1];
            SUM = 0.0;
            for (int J=KQQ; J>0; J--) {
                SUM = SUM + DT[(J-1) + (I-1)*15] * W[J+KS-1];
            }
            STATE[I-1] = REFPOS[I-1] + DELTA * (REFVEL[I-1] + DELTA * SUM);
        }

        for (int J=1; J<JX + 1; J++) {
            W[J + KS - 1] = FC[J] * W[J + KS1 - 1] - WC[J - 1] * W[J + KS - 1];
        }
        KS = KS - 1;

        for (int I=1; I<3 + 1; I++) {
            KQQ = KQ[I - 1];
            SUM = 0.0;
            for (int J = KQQ; J > 0; J--) {
                SUM = SUM + DT[(J - 1) + (I - 1) * 15] * W[J + KS - 1];
            }
            STATE[I + 3 - 1] = REFVEL[I - 1] + DELTA * SUM;
        }

        return STATE;
    }

    /**
     * Initialize with BSP file
     * @param path filepath to .bsp file
     */
    public void initWithBSPFile(String path)
    {
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
            // default endianness
            isLittleEndian = Boolean.TRUE;

            //Get file channel in read-only mode
            FileChannel fileChannel = file.getChannel();

            //Get direct byte buffer access using channel.map() operation
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            //You can read the file from this buffer the way you like.
            buffer.get(bbuf,0,7);
            if ((!isEqualByteArray(bbuf, DAFstr.getBytes(), 7)) && ((!isEqualByteArray(bbuf, NAIFstr.getBytes(), 7))))
            {
                System.out.println("File is not correct");
                return;
            }
            buffer.position(88);
            buffer.get(bbuf,0,8);
            if (isEqualByteArray(bbuf, LEstr.getBytes(), 8))
            {
                //System.out.println("[DEBUG] Little endianness");
                isLittleEndian = Boolean.TRUE;
            }
            else
            {
                //System.out.println("[DEBUG] Big endianness");
                isLittleEndian = Boolean.FALSE;
            }
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
            //assert(nd==2);
            //assert(ni==6);
            //
            int summary_offset = (fward - 1) * RECLEN;
            int summary_size = nd + (ni + 1) / 2; // integer division
            int nxt=1;
            int prv=0;
            int nsum=0;
            while (nxt !=0 )
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
                    }
                    // read ni==6 unsigned int
                    for (int j = 0; j<(int) ni; j++)
                    {
                        buffer.get(bbuf, 0, 4);
                        irec[j] = (int) getUnsignedInt(bbuf);
                    }
                    summary_offset = summary_offset + summary_size * 8; // bytes
                    summary.addRecord(drec[0],drec[1],irec[0],irec[1],irec[2],irec[3],irec[4],irec[5]);
                }
                if (nxt != 0)
                {
                    summary_offset = (nxt - 1) * RECLEN;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error (e.g. opening file, wrong detection of endianness)");
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
    public Vector3D[] getPositionVelocity(double et, int target, int observer)
    {
        Vector3D[] result = new Vector3D[2];
        result[0] = new Vector3D();
        result[1] = new Vector3D();
        int order;
        if (!isInit) {
            System.out.println("SPK::getPositionVelocity, class instance not initialized");
            return result;
        }
        int idx = summary.searchRecord(et, target, observer);
        if (idx == -1) {
            System.out.println("SPK::getPositionVelocity, Cannot find record");
            System.out.println(" et = " + et);
            System.out.println(" target = " + target);
            System.out.println(" observer = " + observer);
            return result;
        }
        int type = summary._type[idx];
        int rbeg = summary._rbeg[idx];
        int rend = summary._rend[idx];
        double px = 0.0, py = 0.0, pz = 0.0;
        double vx = 0.0, vy = 0.0, vz = 0.0;

        if (type == 1) { // Type I interpolation
            // original code by Marco Brasse' based on original DAF/SPK specification
            int M = 71; // specified value for Type-1 interpolation
            int offset = (rend - 1) * 8;
            buffer.position(offset);
            buffer.get(bbuf, 0, 8);
            int n = (int) (getDouble(bbuf));
            int dir_size = n + n / 100;
            assert (n >= 1);
            int s0 = -1; // on purpose, a negative number due to how data is structured in file
            int s1 = n - 1;
            int m;
            double t;
            // binary search in epoch table
            while (s1 != s0 + 1) {
                m = (s0 + s1) / 2; // m>=0
                offset = (rend - (dir_size + 1 - m)) * 8;
                buffer.position(offset);
                buffer.get(bbuf, 0, 8);
                t = getDouble(bbuf);
                if (et > t) s0 = m;
                else s1 = m;
            }
            idx = s0 + 1;
            assert (idx >= 0);
            // Extract parameters of MDA record in file
            double[] p = new double[M];
            for (int i = 0; i < M; i++) {
                offset = (rbeg - 1) * 8 + idx * M * 8 + i * 8;
                buffer.position(offset);
                buffer.get(bbuf, 0, 8);
                p[i] = getDouble(bbuf);
            }
            // compute position and velocity from MDA record
            double[] state = spke01(et, p);
            px = state[0];
            py = state[1];
            pz = state[2];
            vx = state[3];
            vy = state[4];
            vz = state[5];
        }
        else if ((type == 2) || (type==3)) // Type II or III interpolation
        {
            int offset = (rend - 4) * 8;
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
            int record = 8 * (int) (rbeg + internal_offset);

            for (int i = 0; i < (int) rsize; i++) {
                buffer.position(((record - 8) + i * 8));
                buffer.get(bbuf, 0, 8);
                data[i] = (double) getDouble(bbuf);
            }

            if (type==2) { // Type II specific interpolation
                order = (int) (((int) (rsize) - 2) / 3 - 1);
                double tau = (et - data[0]) / data[1];
                int deg = (int) (order + 1);
                double factor = 1.0 / data[1];

                px = chebyshev(order, tau, 2 + 0 * deg);
                py = chebyshev(order, tau, 2 + 1 * deg);
                pz = chebyshev(order, tau, 2 + 2 * deg);
                vx = der_chebyshev(order, tau, 2 + 0 * deg) * factor;
                vy = der_chebyshev(order, tau, 2 + 1 * deg) * factor;
                vz = der_chebyshev(order, tau, 2 + 2 * deg) * factor;
            }
            else { // Type III specific interpolation
                order = (int) (((int) (rsize) - 2) / 6 - 1);
                double tau = (et - data[0]) / data[1];
                int deg = (int) (order + 1);
                px = chebyshev(order, tau, 2 + 0 * deg);
                py = chebyshev(order, tau, 2 + 1 * deg);
                pz = chebyshev(order, tau, 2 + 2 * deg);
                vx = chebyshev(order, tau, 2 + 3 * deg);
                vy = chebyshev(order, tau, 2 + 4 * deg);
                vz = chebyshev(order, tau, 2 + 5 * deg);
            }
        } else {
            System.out.println("SPK::getPositionVelocity, interpolation type in BSP file not recognized");
            // return initialized values (0,0,0,0,0,0)
        }

        // Convert position to m and velocity to m/s
        result[0] = new Vector3D(1000.0*px, 1000.0*py, 1000.0*pz);
        result[1] = new Vector3D(1000.0*vx, 1000.0*vy, 1000.0*vz);
        return result;
    }
}