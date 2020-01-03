/*
 * Java version by Marco Brassé, refactored from original Fortran source:
 * ftp://ftp.imcce.fr/pub/ephem/satel/tass17/
 * Functionally, the Java code only supports TASS1.6 (so without Hyperion)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR I
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ephemeris;

import util.Vector3D;

import java.io.File;
import java.util.*;

/**
 * Main class for feasibility study
 * @author Marco Brassé
 */

public class EphemerisSaturnMoons implements IEphemeris {

    // Factors to correct for velocity
    private double[] correctionVelocity;

    //
    //SUBROUTINE CALCELEM(DJ,IS,ELEM,DLO)
    //
    private void CALCELEM(double aDateJulian, int IS, double[] ELEM, double[] DLO)
    {
        //SUBROUTINE CALCELEM(DJ,IS,ELEM,DLO)
        double T,S,CS,SN,S1,S2;
        double PHAS;
        T=(aDateJulian-2444240.0)/365.25;
        S=0;
        for (int I=1;I<=NTR[1][IS];I++)
        {
            PHAS=getSERIES(I,2,1,IS);
            for (int JK=1;JK<=8;JK++) PHAS=PHAS+getIKS(I,1,IS,JK)*DLO[JK];
            S=S+getSERIES(I,1,1,IS)*Math.cos(PHAS+T*getSERIES(I,3,1,IS));
        }
        ELEM[1]=S;
        S=DLO[IS]+AL0[IS];
        for (int I=NTR[5][IS]+1; I<=NTR[2][IS]; I++)
        {
            PHAS=getSERIES(I,2,2,IS);
            for (int JK=1;JK<=8;JK++) PHAS=PHAS+getIKS(I,2,IS,JK)*DLO[JK];
            S=S+getSERIES(I,1,2,IS)*Math.sin(PHAS+T*getSERIES(I,3,2,IS));
        }
        S=S+AN0[IS]*T;
        CS=Math.cos(S);
        SN=Math.sin(S);
        ELEM[2]=Math.atan2(SN,CS);
        S1=0;
        S2=0;
        for (int I=1;I<=NTR[3][IS];I++)
        {
            PHAS=getSERIES(I,2,3,IS);
            for (int JK=1;JK<=8;JK++) PHAS=PHAS+getIKS(I,3,IS,JK)*DLO[JK];
            S1=S1+getSERIES(I,1,3,IS)*Math.cos(PHAS+T*getSERIES(I,3,3,IS));
            S2=S2+getSERIES(I,1,3,IS)*Math.sin(PHAS+T*getSERIES(I,3,3,IS));
        }
        ELEM[3]=S1;
        ELEM[4]=S2;
        S1=0;
        S2=0;
        for (int I=1;I<=NTR[4][IS];I++)
        {
            PHAS=getSERIES(I,2,4,IS);
            for (int JK=1;JK<=8;JK++) PHAS=PHAS+getIKS(I,4,IS,JK)*DLO[JK];
            S1=S1+getSERIES(I,1,4,IS)*Math.cos(PHAS+T*getSERIES(I,3,4,IS));
            S2=S2+getSERIES(I,1,4,IS)*Math.sin(PHAS+T*getSERIES(I,3,4,IS));
        }
        ELEM[5]=S1;
        ELEM[6]=S2;
        return;
    }
    //
    //SUBROUTINE CALCLON(DJ,DLO)
    //
    private void CALCLON(double aDateJulian, double[] DLO)
    {
        double T;
        T=(aDateJulian-2444240.0)/365.25;
        for (int IS=1; IS<=8; IS++)
        {
            if (IS != 7)
            {
                double S=0;
                for (int I=1; I<=NTR[5][IS]; I++)
                {
                    S=S+getSERIES(I,1,2,IS)*Math.sin(getSERIES(I,2,2,IS)+T*getSERIES(I,3,2,IS));
                }
                DLO[IS]=S;
            }
            else
            {
                DLO[IS]=0.0;
            }
        }
        return;
    }
    //
    //SUBROUTINE EDERED(ELEM,XYZ,VXYZ,ISAT)
    //
    private void EDERED(double[] ELEM, double[] XYZ, double[] VXYZ, int ISAT)
    {
        double EPS=1.0E-14;
        double AMO,RMU,DGA,RL,RK,RH,CF,SF,CORF,FLE;
        double RSAM1,ASR,PHI,PSI,DLF;
        double X1,Y1;
        double VX1,VY1;
        double DWHO,RHO,RTP,RTQ,RDG,CI,SI,CO,SO;
        double[] XYZ2 = new double[3];
        double[] VXYZ2 = new double[3];

        AMO=AAM[ISAT]*(1.0+ELEM[1]);
        RMU=GK1*(1.0+TMAS[ISAT]);
        DGA=Math.pow((RMU/(AMO*AMO)),(1.0/3.0));
        RL=ELEM[2];
        RK=ELEM[3];
        RH=ELEM[4];
        FLE=RL-RK*Math.sin(RL)+RH*Math.cos(RL);
        CORF = EPS*2.0; // hack
        while (Math.abs(CORF)>=EPS)
        {
            CF=Math.cos(FLE);
            SF=Math.sin(FLE);
            CORF=(RL-FLE+RK*SF-RH*CF)/(1.0-RK*CF-RH*SF);
            FLE=FLE+CORF;
        }
        CF=Math.cos(FLE);
        SF=Math.sin(FLE);
        DLF=-RK*SF+RH*CF;
        RSAM1=-RK*CF-RH*SF;
        ASR=1.0/(1.0+RSAM1);
        PHI=Math.sqrt(1.0-RK*RK-RH*RH);
        PSI=1.0/(1.0+PHI);
        X1=DGA*(CF-RK-PSI*RH*DLF);
        Y1=DGA*(SF-RH+PSI*RK*DLF);
        VX1=AMO*ASR*DGA*(-SF-PSI*RH*RSAM1);
        VY1=AMO*ASR*DGA*( CF+PSI*RK*RSAM1);
        DWHO=2.0*Math.sqrt(1.0-ELEM[6]*ELEM[6]-ELEM[5]*ELEM[5]);
        RTP=1.0-2.0*ELEM[6]*ELEM[6];
        RTQ=1.0-2.0*ELEM[5]*ELEM[5];
        RDG=2.0*ELEM[6]*ELEM[5];
        XYZ2[0] =      X1 *   RTP   +  Y1 *   RDG;
        XYZ2[1] =      X1 *   RDG   +  Y1 *   RTQ;
        XYZ2[2] =  ( - X1 * ELEM[6] +  Y1 * ELEM[5] )  *  DWHO;
        VXYZ2[0]=     VX1 *   RTP   + VY1 *   RDG;
        VXYZ2[1]=     VX1 *   RDG   + VY1 *   RTQ;
        VXYZ2[2]=  ( -VX1 * ELEM[6] + VY1 * ELEM[5] )  *  DWHO;
        CI=Math.cos(AIA);
        SI=Math.sin(AIA);
        CO=Math.cos(OMA);
        SO=Math.sin(OMA);
        XYZ[0]=CO *  XYZ2[0] - SO*CI *  XYZ2[1] + SO*SI *  XYZ2[2];
        XYZ[1]=SO *  XYZ2[0] + CO*CI *  XYZ2[1] - CO*SI *  XYZ2[2];
        XYZ[2]=                  SI  *  XYZ2[1] +  CI   *  XYZ2[2];
        VXYZ[0]=CO * VXYZ2[0] - SO*CI * VXYZ2[1] + SO*SI * VXYZ2[2];
        VXYZ[1]=SO * VXYZ2[0] + CO*CI * VXYZ2[1] - CO*SI * VXYZ2[2];
        VXYZ[2]=                  SI  * VXYZ2[1] +  CI   * VXYZ2[2];
    }
    //
    public void POSIRED7(double aDateJulian, int IS, double[] XYZ, double[] VXYZ)
    {
        //DIMENSION XYZ(3),VXYZ(3),ELEM(6),DLO(8)
        double[] ELEM = new double[7];
        double[] DLO = new double[9];
        //if(IS.EQ.7) then
        // CALL ELEMHYP(DJ,ELEM)
        //ELSE
        CALCLON(aDateJulian,DLO);
        CALCELEM(aDateJulian,IS,ELEM,DLO);
        //END IF
        //System.out.println(">"+ELEM[1]+" "+ELEM[2]+" "+ELEM[3]);
        EDERED(ELEM,XYZ,VXYZ,IS);
        //System.out.println("<"+XYZ[0]+" "+XYZ[1]+" "+XYZ[2]);
    }
    //
    private double[][][][] SERIES;
    private int[][][][] IKS;
    private double[] TMAS;
    private double[] AAM;
    private int[][] NTR;
    private double GK;
    private double GK1;
    private double TAS;
    private double AIA;
    private double OMA;
    private double[] AL0;
    private double[] AN0;
    private double RADSDG;
    private double PI;
    //
    private double getSERIES(int a, int b, int c, int d)
    {
        return SERIES[a][b][c][d];
    }
    //
    private int getIKS(int a, int b, int c, int d)
    {
        return IKS[a][b][c][d];
    }

    // First valid date
    private final GregorianCalendar firstValidDate;

    // Last valid date
    private final GregorianCalendar lastValidDate;

    // First valid date for accurate computation
    private final GregorianCalendar firstValidDateAccurate;

    // Last valid date for accurate computation
    private final GregorianCalendar lastValidDateAccurate;

    // Bodies for which ephemeris can be computed or approximated
    private static List<String> bodies;

    // Singleton instance
    private static IEphemeris instance = null;

    //
    // Javafied Fortran code for reading data file
    // saturnMoonsEphemeris.txt
    //
    private final void LECSER()
    {

        File ephemerisData;
        Scanner scnr;
        RADSDG=Math.atan(1.0)/45.0;
        PI=180.0*RADSDG;
        //creating File instance to reference text file in Java
        //Creating Scanner instance to read File in Java

        try {
            // ephemerisData = new File("./saturnMoonsEphemeris.txt");
            // File path adapted by Nico Kuijpers (September 21, 2019)
            ephemerisData = new File("EphemerisFiles/saturnMoonsEphemeris.txt");
            scnr = new Scanner(ephemerisData);
        }
        catch (Exception e) {
            System.out.println("Exception occurred: file not found");
            return;
        }

        if (scnr.hasNextLine()){
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            GK = s.nextDouble();
        }
        if (scnr.hasNextLine()){
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            TAS = s.nextDouble();
        }
        GK1=(GK*365.25)*(GK*365.25)/TAS;
        if (scnr.hasNextLine()){
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            AIA = s.nextDouble();
            AIA = AIA*RADSDG;
            OMA = s.nextDouble();
            OMA = OMA*RADSDG;
        }
        if (scnr.hasNextLine()){
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            for (int i=1;i<=9;i++)
            {
                double TAM=s.nextDouble();
                TMAS[i] = 1.0/TAM;
            }
        }
        if (scnr.hasNextLine()){
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            for (int i=1;i<=9;i++)
            {
                double AM=s.nextDouble();
                AAM[i] = AM*365.25;
            }
        }
        int RESULT;
        RESULT = 0;
        int IS,IEQ;
        while (scnr.hasNextLine()) {
            int NT;
            int KT = 0;
            int KT_MAX = 250;
            String line = scnr.nextLine();
            Scanner s = new Scanner(line);
            s.useLocale(Locale.ENGLISH);
            IS = s.nextInt();
            IEQ = s.nextInt();
            if (7==IS) {
                // 7 denotes moon Hyperion.
                // near future work (todo): process moon Hyperion.
                // Currently only TASS 1.6 is supported (not 1.7),
                // which does not include moon Hyperion.
                // Hyperion is the last moon in fortran data file,
                // and is processed differently from other 7 moons
                RESULT = 1;
                break;
            }
            if (2==IEQ) {
                if (scnr.hasNextLine()) {
                    line = scnr.nextLine();
                    s = new Scanner(line);
                    s.useLocale(Locale.ENGLISH);
                    NT = s.nextInt();
                    AL0[IS] = s.nextDouble();
                    AN0[IS] = s.nextDouble();
                }
            }
            while (scnr.hasNextLine()) {
                line = scnr.nextLine();
                s = new Scanner(line);
                s.useLocale(Locale.ENGLISH);
                NT = s.nextInt();
                if (9998>NT) {
                    int IK[] = new int[9];
                    double A1 = s.nextDouble();
                    double A2 = s.nextDouble();
                    double A3 = s.nextDouble();
                    IK[0]=0;
                    for (int JS=1;JS<=8;JS++) {
                        IK[JS] = s.nextInt();
                    }
                    if (KT < KT_MAX) {
                        KT = KT + 1; // compatible with Fortran
                        SERIES[KT][1][IEQ][IS] = A1;
                        SERIES[KT][2][IEQ][IS] = A2;
                        SERIES[KT][3][IEQ][IS] = A3;
                        for (int JS=1;JS<=8;JS++)
                        {
                            IKS[KT][IEQ][IS][JS] = IK[JS];
                        }
                    }
                }
                else if (9998==NT)
                {
                    if (2==IEQ) NTR[5][IS]=KT;
                }
                else if (9999==NT)
                {
                    if ((2==IEQ) && (0==NTR[5][IS])) NTR[5][IS]=KT;
                    NTR[IEQ][IS]=KT;
                    break;
                }
            }
        }
        if (RESULT != 1) {
            System.out.println("!!Error in reading Saturn moon data file.");
        }
    }
    /**
     * Constructor. Singleton pattern.
     */
    private EphemerisSaturnMoons() {


        // Bodies for which ephemeris can be computed or approximated
        bodies = new ArrayList<>();
        bodies.add("Mimas");
        bodies.add("Enceladus");
        bodies.add("Tethys");
        bodies.add("Dione");
        bodies.add("Rhea");
        bodies.add("Titan");
        //bodies.add("Hyperion") // not supported in this class yet
        bodies.add("Iapetus");

        // First valid date 3000 BC
        firstValidDate = new GregorianCalendar(3000,0,1);
        firstValidDate.set(Calendar.ERA, GregorianCalendar.BC);
        firstValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date 3000 AD
        lastValidDate = new GregorianCalendar(3000,0,1);
        lastValidDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        // First valid date for accurate computation 1150 AD
        firstValidDateAccurate = new GregorianCalendar(1150,0,1);
        firstValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));

        // Last valid date for accurate computation 2750 AD
        lastValidDateAccurate = new GregorianCalendar(2750,0,1);
        lastValidDateAccurate .setTimeZone(TimeZone.getTimeZone("UTC"));

        // Standard gravitional parameters
        double au = SolarSystemParameters.ASTRONOMICALUNIT;
        double nrSecsDay = 86400.0;
        //
        // initalize javafied Fortran variables
        //
        SERIES = new double[251][4][5][9];
        IKS = new int[251][5][9][9];
        TMAS = new double[10];
        AAM = new double[10];
        NTR = new int[6][9];
        AL0 = new double[9];
        AN0 = new double[9];
        // read and process Fortran data file
        LECSER();
    }

    /**
     * Get instance of EphemerisSaturnMoons.
     * @return instance
     */
    public static IEphemeris getInstance() {
        if (instance == null) {
            instance = new EphemerisSaturnMoons();
        }
        return instance;
    }

    @Override
    public GregorianCalendar getFirstValidDate() {
        return firstValidDate;
    }

    @Override
    public GregorianCalendar getLastValidDate() {
        return lastValidDate;
    }

    @Override
    public List<String> getBodies() {
        return Collections.unmodifiableList(bodies);
    }

    @Override
    public Vector3D getBodyPosition(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[0];
    }

    @Override
    public Vector3D getBodyVelocity(String name, GregorianCalendar date) {
        return getBodyPositionVelocity(name,date)[1];
    }

    @Override
    public Vector3D[] getBodyPositionVelocity(String name, GregorianCalendar date) {
        // Check whether body name is valid
        if (!bodies.contains(name)) {
            throw new IllegalArgumentException("Unknown body " + name + " for Galilean Moons Ephemeris");
        }

        // Check whether date is valid
        if (date.before(firstValidDate) || date.after(lastValidDate)) {
            throw new IllegalArgumentException("Date not valid for Galilean Moons Ephemeris");
        }

        // Compute Ephemeris Time
        double ET = JulianDateConverter.convertCalendarToJulianDate(date);

        // Determine satellite number
        int nsat;
        if ("Mimas".equals(name)) nsat = 1;
        else if ("Enceladus".equals(name)) nsat = 2;
        else if ("Tethys".equals(name)) nsat = 3;
        else if ("Dione".equals(name)) nsat = 4;
        else if ("Rhea".equals(name)) nsat = 5;
        else if ("Titan".equals(name)) nsat = 6;
            //else if ("Hyperion".equals(name)) nsat = 7; // not supported yet
        else if ("Iapetus".equals(name)) nsat = 8;
        else /* choose Titan as default */ nsat = 6;

        // Check whether Accurate Ephemeris can be used
        if (!date.before(firstValidDateAccurate) && !date.after(lastValidDateAccurate)) {
            return getPositionVelocity(ET, nsat, 0);
        }
        else {
            return getPositionVelocity(ET, nsat, 0);
        }
    }

    @Override
    public Vector3D getBodyPositionBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D getBodyVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Vector3D[] getBodyPositionVelocityBarycenter(String name, GregorianCalendar date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Vector3D[] getPositionVelocity(double ET, int nsat, int not_used)
    {
        //!*  From original explanation in Fortran code:
        //!*  ET : Time in Julian days (TDB) )
        //!*  nsat: satellite number 1,2,3,4,5,6, or 8 (7 not supported)
        //!*  return: (x,y,z,vx,vy,vz), Saturn centered Cartesian coordinates
        //!*  relative to the Saturn and mean ecliptic/equinox for J2000.0 epoch
        //
        // Positions and velocities of the satellites Mimas (1), Enceladus (2),
        // Tethys (3), Dione (4), Rhea (5), Titan (6), (Hyperion) and
        // Iapetus (8)referred to the center of Saturn and to the mean
        // ecliptic and mean equinox for J2000.0 epoch
        //
        // It is based on a numerical model called TASS 1.6 [ref]
        double UA = SolarSystemParameters.ASTRONOMICALUNIT; // m
        double SEC_PER_YR=86400.0*365.25;
        double[] XYZ = new double[3];
        double[] VXYZ = new double[3];

        // call Javafied Fortran subroutine
        POSIRED7(ET, nsat, XYZ, VXYZ);

        // Position in m, already in J2000 mean ecliptic coordinates
        Vector3D position = new Vector3D(XYZ[0]*UA, XYZ[1]*UA, XYZ[2]*UA);
        //Vector3D position = new Vector3D(XYZ[0], XYZ[1], XYZ[2]);

        // Velocity in m/s, already in J2000 mean ecliptic coordinates
        double factor = UA/SEC_PER_YR;
        Vector3D velocity = new Vector3D(VXYZ[0]*factor, VXYZ[1]*factor, VXYZ[2]*factor);

        return new Vector3D[]{position,velocity};
    }

    public static void main (String[] args) {
        Vector3D[] posVel = new Vector3D[2];
        EphemerisSaturnMoons obj=new EphemerisSaturnMoons();
        //posVel=obj.getPositionVelocity(  2421677.4, 1, 0);
        //posVel=obj.getPositionVelocity(  2444555.8, 4, 0);
        //posVel=obj.getPositionVelocity(  2406147.5, 2, 0);
        posVel=obj.getPositionVelocity(2440512.6, 6, 0);
        //posVel=obj.getPositionVelocity(2443569.3, 6, 0);
        //posVel=obj.getPositionVelocity( 2444555.8, 6, 0);
        //posVel=obj.getPositionVelocity( 2443179.7, 8, 0);
        System.out.println("Resulting positionVelocity");
        System.out.println(posVel[0]);
        System.out.println(posVel[1]);
    }
}

