import javafx.geometry.Point3D;
import java.util.*;
import java.util.HashSet;
import java.util.Set;
import java.text.DecimalFormat;
import javafx.util.Pair;
import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;
import java.io.BufferedWriter;
class User {
        int UID;
        int status;
        double SINR=0.0;
        double totalSINR=0.0;
        Point3D loc;
        BS target;
        double bitrate=0.0;
        double totalbitrate=0.0;
        int initialEpoch;
        int nextEpoch;
        int epochPeriod;
        HashMap<Integer, Pair<Integer,Pair<Double,Pair<Double,Double>>>> hmap = new HashMap<>();
    public User(int UID, Point3D loc,int epochFreq,int initialEpoch) {
        
        this.UID = UID;
        this.loc = loc;
        this.target = null;
        this.status = 0;
        this.nextEpoch=initialEpoch;
        this.epochPeriod=epochFreq;
        this.bitrate=0.0;
        this.SINR=0.0;
    }
    public void clearUser()
    {
        this.nextEpoch=this.initialEpoch;
        this.bitrate=0.0;
        this.hmap.clear();
        this.totalbitrate=0.0;
        this.totalSINR=0.0;
        this.status=0;
        this.SINR=0.0;
        this.totalSINR=0.0;
        this.target=null;
    }
};
class Parameter {
    public static final int noOfIteration=1000;
    public static final double setInertia=0.6;
    public static final double pLC=0.3;//0.3,0.7,0.3
    public static final double gLC=0.7;//0.7,0.7,0.3
    public static final int totaltime=1000;
	public static final int no_bs=10;
	public static final int no_ue=100;
    public static final double BW = 10;
    public static final double RB = 50;
    public static final double TXF = 0.1;         //  Total : 26 dBm (0.4 watt), Per subchannel : 0.4/50 = 0.008 watt
    public static final double NOISE = 0.0000000000001;
    public static final double WAVELENGTH = (double)1800000000/3000000; //Frequency band 1.8GHz/Speed of light

};
class BS {
    int BID;
    double radius;
    Point3D loc;
    double velocity;
    double angle;
    boolean direction;
    Set<User> associatedUE = new HashSet<>();
    double power;
    BS(int BID,double radius,Point3D loc,double velocity,boolean dir,double power)
    {
        this.power=power;
        this.BID=BID;
        this.radius=radius;
        this.loc=loc;
        this.velocity=velocity;
        this.direction=dir;
        this.associatedUE.clear();
    }
    public Point3D curloc(int time)
    {
        double theta=Math.toRadians(this.velocity/this.radius);
        //System.out.println(theta);

        double x=this.loc.getX()+this.radius*Math.cos(theta*time); 
        double y=this.loc.getY()+(this.radius*Math.sin(theta*time));
        Point3D cur=new Point3D(x,y,this.loc.getZ());
        return cur;
    }
    public void clearBS()
    {

        this.associatedUE.clear();
    }  
};
public class PSO_INIT{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {



        Vector<Double> powerOfBS=new Vector<>();
        Set<User> setUE = new HashSet<>();
        Set<BS> setBS = new HashSet<>();
        Vector<Double>Velocity=new Vector<>();
        Vector<Double>Radius=new Vector<>();
        Random r = new Random(70);
        Random d = new Random(50);
        Random k = new Random(30);
        for(int i=0;i<Parameter.no_bs;i++)
        {
            
            double v = r.nextDouble()*100;
           // System.out.println("Velocity:"+v);
            double z = d.nextDouble()*100;
           // System.out.println("Radius:"+z);
            Velocity.add(v);
            Radius.add(z);
            powerOfBS.add(0.9);
        }
        Random r1 = new Random(111);//80
        Random p= new Random(70);
        for(int i=0;i<Parameter.no_ue;i++)
        {   
            double x = r1.nextDouble()*100;
            double y = r1.nextDouble()*100;
            double z = r1.nextDouble()*100;
            int st=r1.nextInt();
            //System.out.println("x: "+x+"y: "+y);
            int epochFreq=p.nextInt(50);
          //  System.out.println("epoch for :"+i+": "+epochFreq);
            Point3D loc = new Point3D(x,y,z);
            setUE.add(new User(i,loc,epochFreq,st));
        }
        Random r2 = new Random(1);//40
        for(int i=0;i<Parameter.no_bs;i++)
        {
            double x = r2.nextDouble()*100;
            double y = r2.nextDouble()*100;
            double z = r2.nextDouble()*100;
           // System.out.println(z);
            Point3D loc = new Point3D(x,y,z);
            boolean dir=false;
            int rd=r2.nextInt();
            if(rd%2==0)
                dir=true;
            else
                dir=false;
            setBS.add(new BS(i,Radius.get(i),loc,Velocity.get(i),dir,powerOfBS.get(i)));      
        }

       
        /*
            for (BS b :setBS ){
                System.out.println("Base Station :"+b.BID+".......");
                 for (int g=0;g<1000;g++) {
                    Point3D cur=b.curloc(g);
                    System.out.println("X : "+cur.getX()+"  Y : "+cur.getY()+"  Z : "+cur.getZ());
                }
            }
        */
      
        double m1=Call(setUE,setBS,1,"method1.txt");// min distance
         for (User u :setUE ) {
                System.out.println("UID : "+u.UID+" totalbitrate : "+u.totalbitrate+" totalSINR : "+u.totalSINR );  
            }
        double m2=Call(setUE,setBS,2,"method2.txt");// min avg distance
         for (User u :setUE ) {
                System.out.println("UID : "+u.UID+" totalbitrate : "+u.totalbitrate+" totalSINR : "+u.totalSINR );  
            }
        double m3=Call(setUE,setBS,3,"method3.txt");// max avg signal 
         for (User u :setUE ) {
                System.out.println("UID : "+u.UID+" totalbitrate : "+u.totalbitrate+" totalSINR : "+u.totalSINR );  
            }
        double m4=Call(setUE,setBS,4,"method4.txt");// max avg sinr 
         for (User u :setUE ) {
                System.out.println("UID : "+u.UID+" totalbitrate : "+u.totalbitrate+" totalSINR : "+u.totalSINR );  
            }
        System.out.println("method 1 "+m1);
        System.out.println("method 2 "+m2);
        System.out.println("method 3 "+m3);
        System.out.println("method 4 "+m4);

       // callPSO(setUE,setBS,1,"method11.txt");// min distance
       // callPSO(setUE,setBS,2,"method22.txt");// min avg distance
       // callPSO(setUE,setBS,3,"method33.txt");// max avg signal 
        callPSO(setUE,setBS,4);// max avg sinr 

        for (BS b :setBS ) {
            System.out.println(b.BID+" : "+b.power);               
        }
        double m11=Call(setUE,setBS,1,"method11.txt");// min distance
        double m22=Call(setUE,setBS,2,"method22.txt");// min avg distance
        double m33=Call(setUE,setBS,3,"method33.txt");// max avg signal 
        double m44=Call(setUE,setBS,4,"method44.txt");// max avg sinr 
        System.out.println("method 1 "+m11);
        System.out.println("method 2 "+m22);
        System.out.println("method 3 "+m33);
        System.out.println("method 4 "+m44);           
    }
    private static double totalBW(Set<User>setUE,Set<BS>setBS,Pair<Integer,Double>P,int j)
    {
        for (BS b :setBS) {
            if(b.BID==P.getKey())
            {
                b.power=P.getValue();
            }
        }
       // Call(setUE,setBS,1,"method1.txt");// min distance
       // Call(setUE,setBS,2,"method2.txt");// min avg distance
       // Call(setUE,setBS,3,"method3.txt");// max avg signal 
        return Call(setUE,setBS,j,"method5");// max avg sinr 

    }
    private static void callPSO(Set<User>setUE,Set<BS>setBS,int j)
    {
        HashMap<Integer,Pair<Double,Double>> grid= new HashMap<>();
        HashMap<Integer,Double>global_best=new HashMap<>();
        HashMap<Integer,Double>local_best=new HashMap<>();
        Random r1=new Random(2);
        for (int i=0;i<Parameter.no_bs;i++) 
        {
            //double k=(r1.nextDouble()*100)/1000;
            global_best.put(i,0.9);
            //System.out.println(i+" : "+best_pos.get(i));
        }
        int nOI=Parameter.noOfIteration;
        double inertia=Parameter.setInertia;//0.6(w)
        double pLC=Parameter.pLC;//0.6(phi-p)
        double gLC=Parameter.gLC;//1.0(phi-g)
        double current=Call(setUE,setBS,j,"method5");
        double previous;
        for (int i=0;i<Parameter.no_bs;i++) 
        {
            
            double k=Math.random();
            Pair<Integer,Double>pt=new Pair<>(i,k);
            double cd=(Math.random()*2)-1;
            Pair<Double,Double> p=new Pair(k,cd);
            grid.put(i,p);
            local_best.put(i,k);
            double pb=global_best.get(i);
            Pair<Integer,Double>pg=new Pair<>(i,pb);
            if(totalBW(setUE,setBS,pt,j)>totalBW(setUE,setBS,pg,j))
            {
                global_best.put(i,k);
            }
        }
        int iterator=1;
        do
        {   previous=current;
            for (int i=0;i<Parameter.no_bs;i++) 
            {
                double rp=Math.random();
                double rg=Math.random();
                Pair c=grid.get(i);
                double vi=inertia*(double)c.getValue()+pLC*rp*(local_best.get(i)-(double)c.getKey())+gLC*rg*(global_best.get(i)-(double)c.getKey());
                double po=(double)c.getKey()+vi;
                double lb=local_best.get(i);
                double gb=global_best.get(i);
                Pair<Integer,Double> pop=new Pair<>(i,po);
                Pair<Integer,Double> gbp=new Pair<>(i,gb);
                Pair<Integer,Double> lbp=new Pair<>(i,lb);
                Pair d=new Pair<>(po,vi);
                grid.put(i,d);
                double poBW=totalBW(setUE,setBS,pop,j);
                double lbBW=totalBW(setUE,setBS,lbp,j);
                double gbBW=totalBW(setUE,setBS,gbp,j);
                if(poBW>lbBW)
                {
                    local_best.put(i,po);
                }
                if(poBW>gbBW)
                {
                    global_best.put(i,po);
                }
            }
            iterator++;

                for (Map.Entry<Integer,Double>entry : global_best.entrySet())
                {
                    int v=(int)entry.getKey();
                    double c=(double)entry.getValue();
                    for (BS b :setBS ) 
                    {
                        if(b.BID==v)
                        {
                            b.power=c;
                        }
                    }
                }
            current=Call(setUE,setBS,j,"method5");
        }while((current-previous)>1&&iterator<nOI);
        for (Map.Entry<Integer,Double>entry : global_best.entrySet())
        {
            int v=(int)entry.getKey();
            double c=(double)entry.getValue();
            for (BS b :setBS ) {
                if(b.BID==v)
                {
                    b.power=c;
                }
            }
        }
    
    }
    private static double Call(Set<User>setUE,Set<BS>setBS,int j,String name)
    {


        double totalBW=0;
        try 
        {
            File myObj = new File(name);
            if (myObj.createNewFile()) 
            {
                System.out.println("File created: " + myObj.getName());
            } 
            else 
            {
                System.out.println("File already exists.");
            }
        }
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        boolean appendToExistingFile = false;
        
        try(BufferedWriter myWriter = new BufferedWriter(new FileWriter(name, appendToExistingFile))) 
        {       
                
                System.out.println("-------For Method"+j+":----------");
                String line = String.format("%s%s%s\n", "-------For Method",j,":----------");
               // myWriter.append(line);
                ClearData(setUE,setBS);
                Sim(setUE,setBS,j,myWriter);//method j
                String lk=String.format("%s\n", "Simulation:----------end.....");
                //myWriter.append(lk);
                EachUserData(setUE,setBS,myWriter);
                
                double totalSINR=0;
                for (User u:setUE ) 
                {
                    String l1 = String.format("%s%s%s\t%s%s\t%s\n","UID : ",u.UID," Total Bitrate ",u.totalbitrate," Total SINR ",u.totalSINR);
                    //myWriter.append(l1);
                    totalBW+=u.totalbitrate;
                    totalSINR+=u.totalSINR;
                } 
                String lin = String.format("%s%s%s%s%s%s%s%s\n","Total bitrate recieved in method",j," is ",totalBW,"Total SINR recieved in method j",j,"is",totalSINR);
                myWriter.append(lin);
                myWriter.close();
                //System.out.println("Successfully wrote to the file.");
                
        } 
        catch (IOException e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }   
        return totalBW;
    }
    private static void ClearData(Set<User> setUE,Set<BS>setBS)
    {
       
        for (User u :setUE ) 
        {
            if(u.target!=null)
            {
                u.target.associatedUE.remove(u); 
            }   
            u.clearUser();
        }
        for (BS b :setBS ) 
        {
            b.clearBS();           
        }
    }
    private static void ChangeStatus(Set<User>setUE, int time) 
    {
        
        for(User u : setUE) 
        {
            if(u.nextEpoch==time)
            {
                if(u.status == 0)
                {
                    u.status = 1;
                    u.nextEpoch = time + u.epochPeriod;
                }
                else 
                {
                    u.status = 0;
                    u.nextEpoch = time + u.epochPeriod;
                    u.target.associatedUE.remove(u);
                    u.totalbitrate=u.totalbitrate+u.bitrate;
                    u.totalSINR=u.totalSINR+u.SINR;
                    u.SINR=0;
                    u.bitrate = 0;// store this bitrate for cdf plot
                    u.target = null;        
                }
            }
        }    
    }
    private static void AssociateUEtoBS1(Set<User> setUE, Set<BS> setBS,int time,BufferedWriter myWrite) 
    {
       for(User u:setUE)
       {
        
            if(u.status==1&&u.target==null) 
            {
                BS target=null;
                double dist=Double.MAX_VALUE;
                for (BS b :setBS ) 
                {
                    Point3D cur= b.curloc(time);
                    Double curdist=u.loc.distance(cur);
                    if (curdist<dist) 
                    {
                        dist = curdist;
                        target = b;
                    }
                }
                if(target!=null)
                {
                    u.target=target;
                    target.associatedUE.add(u);
                }
            }
        }  
    }
    private static void AssociateUEtoBS2(Set<User> setUE, Set<BS> setBS,int time,BufferedWriter myWrite) 
    {
        DecimalFormat df = new DecimalFormat("#.##");
        for(User u:setUE)
        {
            if(u.status==1&&u.target==null) 
            { 
                BS target=null;
                double maxRSRP=-1;
                for (BS b :setBS ) 
                {
                    double sum=0;
                    sum=findavg(u,b,time);
                    double signal = b.power/(Math.pow((4*Math.PI*sum/Parameter.WAVELENGTH), 2.0));
                    if (signal>maxRSRP) 
                    {
                        maxRSRP=signal;
                        target=b;
                    }
                   // System.out.println("Signal: "+u.UID+" and "+b.BID+" "+signal);
                }
                if(target!=null)
                {
                    u.target=target;
                    target.associatedUE.add(u);
                }
                //System.out.println(target.BID);
            }

        }  
    }
    private static void AssociateUEtoBS3(Set<User> setUE, Set<BS> setBS,int time,BufferedWriter myWriter) 
    {
        DecimalFormat df = new DecimalFormat("#.##");
        try
        {
            for(User u:setUE)
            {
            
            String li=String.format("%s%s%s%s\n","For UID ",u.UID," At time: ",time);
            // myWriter.append(li);  
            if(u.status==1&&u.target==null) 
            { 
                BS target=null;
                double maxRSRP=-1;
                for (BS b :setBS ) 
                {
                    double sum=0;
                    sum=findavgsignal(u,b,time);
                    if (sum>maxRSRP) 
                    {
                        maxRSRP=sum;
                        target=b;
                    }
                   
                    if(target!=null)
                    {    
                    String line=String.format("%s%s%s%s%s%s\n","BID: ",b.BID,"  maxRSRP: ",sum,"  target: ",target.BID);
                    //myWriter.append(line);
                    }
                    //System.out.println("Signal: "+u.UID+" and "+b.BID+" "+sum);
                }
                if(target!=null)
                {
                    u.target=target;
                    target.associatedUE.add(u);
                    String line=String.format("%s%s%s%s%s%s\n","UID: ",u.UID,"  maxRSRP: ",maxRSRP,"  target: ",target.BID);
                    //myWriter.append(line);
                }
                //System.out.println(target.BID);
            }
    
            }
        }
        catch(Exception e)
        {

        }
           
    }
    private static void AssociateUEtoBS4(Set<User> setUE, Set<BS> setBS,int time,BufferedWriter myWriter) 
    {
        DecimalFormat df = new DecimalFormat("#.##");
        for(User u:setUE)
        {
            try{
                String li=String.format("%s%s%s%s\n","For UID ",u.UID," At time: ",time);
                      //  myWriter.append(li);
            }
            catch(Exception e)
            {

            }

            if(u.status==1&&u.target==null) 
            {
                BS target=null;
                double maxSINR = -1.0;
               /* for (BS b :setBS ) {
                    for (BS b1 :setBS ) {

                        double sig=0,inter=0;
                        if(b.BID==b1.BID)
                        {
                            sig=findavgsignal(u,b,time);
                        }
                        else
                        {
                            inter+=findavgsignal(u,b,time);
                        }
                        double curSINR=sig/(inter+Parameter.NOISE);
                        if(curSINR>maxSINR)
                        {
                            maxSINR=curSINR;
                            target=b;
                        }
                    }
                    
                }
                */

                HashMap<BS,Double> hmap = new HashMap<>();
                hmap.clear();
                for (BS b :setBS ) 
                {

                    double avgsignal=0;
                    avgsignal=findavgsignal(u,b,time);
                    hmap.put(b,avgsignal);
                }
                for (Map.Entry<BS, Double> e : hmap.entrySet())
                {
                    double total=0;
                    for (Map.Entry<BS,Double>e1 :hmap.entrySet()){
                        if(e.getKey().BID!=e1.getKey().BID)
                        {
                            double loca=e.getValue()/(e1.getValue()+Parameter.NOISE);
                            total+=loca;
                        }
                    }
                    double SINRavg=total/(hmap.size()-1);
                    
                   //System.out.println("SINR for UID :"+u.UID+" and BID: "+e.getKey().BID+" is: "+SINR);
                    if (SINRavg>maxSINR) 
                    {
                        maxSINR= SINRavg;
                        target = e.getKey();
                    }
                    try{ 
                        
                        if(target!=null)
                        {
                            String line=String.format("%s%s%s%s%s%s\n","BID: ",e.getKey().BID," SINR: ",SINRavg,"  target: ",target.BID);
                           // myWriter.append(line);
                        }
                        else
                        {
                            String line2=String.format("%s%s%s%s%s\n","BID: ",e.getKey().BID,"SINR: ",SINRavg,"  target: Null");
                           // myWriter.append(line2);
                        }
                    }
                    catch(Exception l)
                    {

                    }
                }
                if(target!=null)
                {
                    u.target=target;
                    target.associatedUE.add(u);
                }
                //System.out.println(target.BID);
                try{
                String line1=String.format("%s%s%s%s%s%s\n","Final for UID ",u.UID ,"  maxSINR: ",maxSINR,"  target: ",u.target.BID);
               // myWriter.append(line1);
                }
                catch(Exception l)
                {}   
            }

        }   
    }
    private static  double findavg(User u,BS b,int time)
    {
        double sum=0;
        //System.out.println("distance from uid: "+u.UID+" from bs: "+b.BID+" from "+time+" to"+u.nextEpoch);
        for(int i=time;i<=u.nextEpoch;i++)
        {
            Point3D cur= b.curloc(i);
            double p=u.loc.distance(cur);
            //System.out.println(p);
            sum+=p;
        }
        double avg=sum/(u.nextEpoch-time+1);
        //System.out.println("Avg for uid: "+u.UID+" "+avg);
        return avg;
    }
     private static  double findavgsignal(User u,BS b,int time)
    {
        double signal=0;
        double avg;
        //System.out.println("distance from uid: "+u.UID+" from bs: "+b.BID+" from "+time+" to"+u.nextEpoch);
        for(int i=time;i<=u.nextEpoch;i++)
        {
            Point3D cur= b.curloc(i);
            double p=u.loc.distance(cur);
            signal += b.power/(Math.pow((4*Math.PI*p/Parameter.WAVELENGTH), 2.0));
            //System.out.println(p);
        }
        avg=signal/(u.nextEpoch-time+1);
        //System.out.println("Avg for uid: "+u.UID+" "+avg);
        return avg;
    }
    private static void EachUserData(Set<User> setUE, Set<BS> setBS,BufferedWriter myWriter)
    {
        for (User u :setUE ) 
        { 
           try
           {
            
            TreeMap<Integer,Pair<Integer,Pair<Double,Pair<Double,Double>>>> sorted = new TreeMap<>();
            // Copy all data from hashMap into TreeMap 
            sorted.putAll(u.hmap); 
            //DecimalFormat df = new DecimalFormat("#.##");
            //System.out.println("-------Details about UE : "+u.UID);
            String line=String.format("%s%s\n","-------Details about UE : ",u.UID);
            myWriter.append(line);
            double tbr=0;
            double tsinr=0;
            for (Map.Entry m : sorted.entrySet()) 
            {
                Pair d=(Pair) m.getValue();
                Pair c= (Pair) d.getValue();
                Pair e=(Pair)c.getValue();
                double sinr=(double)e.getKey()/((double)e.getValue()+Parameter.NOISE);
                String li=String.format("%s%s%s%s%s%s%s%s%s%s%s%s\n","At time : ",m.getKey()," BID : ",d.getKey()," Bitrate : ",c.getKey()," Signal :",e.getKey()," Interfernce : ",e.getValue()," SINR : ",sinr);
                tbr=tbr+(double)c.getKey();
                tsinr+=sinr;
                myWriter.append(li);
            }  
                u.totalSINR+=tsinr;
                u.totalbitrate+=tbr;
                String li=String.format("%s%s%s%s\n","Total bitrate received for UE : ",u.UID," is : ",tbr);
                myWriter.append(li);
                String l=String.format("%s%s%s%s\n","Total SINR received for UE : ",u.UID," is : ",tsinr);
                myWriter.append(l);

            }
            catch (Exception e) 
            {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
    private static void Sim(Set<User> setUE, Set<BS> setBS,int method,BufferedWriter myWriter)
    {
        try
        {
            for(int time=0;time<Parameter.totaltime;time++)
            {
                // Change status of each UAV from active to idle or idle to active if time
                // equal to next epoch
                ChangeStatus(setUE, time);
                if(method==1)
                    AssociateUEtoBS1(setUE,setBS,time,myWriter);//method one for user association based on max Signal at this time
                if(method==2)
                    AssociateUEtoBS2(setUE,setBS,time,myWriter);//method two for user association based on max avg distance till nextEpoch
                if(method==3)
                    AssociateUEtoBS3(setUE,setBS,time,myWriter);//method three for user association based on max avg SINR nextEpoch
                if(method==4)
                    AssociateUEtoBS4(setUE,setBS,time,myWriter);//method four for user association based on max avg signal till nextEpoch
            
                String line=String.format("%s%s\n","--------UE Details----------- at time = ",time);
                myWriter.append(line);
                UEDetails(setUE,myWriter);// Print details of each UE
                BSDetails(setUE,setBS,time,myWriter);// Print details of each BS
                Stats(setUE, setBS,time,myWriter);// Print details for each BS and UE 
            }
           
        }
        catch (Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
    }
    private static void UEDetails(Set<User>setUE,BufferedWriter myWriter)
    {
        try
        {
            for(User u : setUE)
            {    
                if(u.target==null)
                {
                    String line=String.format("%s%s\t%s%s\t%s%s\n","UID: ",u.UID,"Status: ",u.status,"Target BS: ",u.target);
                    myWriter.append(line);
                    
                }
                else
                {
                    String line=String.format("%s%s\t%s%s\t%s%s\n","UID: ",u.UID,"Status: ",u.status,"Target BS: ",u.target.BID);
                    myWriter.append(line);
                }
            }

        }
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }       
    }
    private static void BSDetails(Set<User>setUE,Set<BS>setBS,int time,BufferedWriter myWriter)
    {
       try{
            for (BS b :setBS ) 
            {
                String line=String.format("%s%s%s%s\n","--------BS Details----------- at time = ",time," for BS",b.BID);
                myWriter.append(line);
                for (User u :b.associatedUE ) 
                {
                    String lin=String.format("%s\n",u.UID);
                    myWriter.append(lin);
                }
            }
        }
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    
    }
    private static void Stats(Set<User> setUE, Set<BS> setBS,int time,BufferedWriter myWriter) 
    {
        DecimalFormat df = new DecimalFormat("#.##"); // For printing float numbers with 2 decimal places
        double througput = 0;
        try
        {
            for(User u : setUE) 
            {
                if(u.status==0)
                continue;
                double signal=0.0, interf = 0.0;
                for(BS b : setBS) 
                {
                    Point3D cur= b.curloc(time);
                    double dist = u.loc.distance(cur);
                    if(b.BID==u.target.BID)
                    {
                        signal = b.power/(Math.pow((4*Math.PI*dist/Parameter.WAVELENGTH), 2.0));
                    }
                    else 
                    {
                        interf+= b.power/(Math.pow((4*Math.PI*dist/Parameter.WAVELENGTH), 2.0));
                    }
                }         
                double SINR = signal/(interf+Parameter.NOISE);
                double SINRDB = 10.0*Math.log10(SINR);
                double bitrate=0;
                bitrate = (Parameter.BW/u.target.associatedUE.size())*Math.log10(1+SINR)/0.3010;
                if(u.target!=null)
                {   
                    u.hmap.put(time,new Pair <> (u.target.BID,new Pair<> (bitrate,new Pair<>(signal,interf))));
                }
                String line=String.format("%s%s%s%s%s%s%s%s%s%s\n","UID: ",u.UID,"  Target BS: ",u.target.BID,"  SINR: ",SINR," dB" , "  Bitrate: ",bitrate," Mbps");
                myWriter.append(line);
            }
           
        }
        catch (Exception e) 
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }     
    }
}