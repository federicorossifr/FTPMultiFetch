package MultiFTPFetcher;
import com.thoughtworks.xstream.*;
import java.io.*;
import org.apache.commons.net.ftp.*;
import java.text.*;
import java.util.Date;
public class MultiFTPFetcher {
    public static void main(String[] args) {
        (new MainWorker()).run();
    }

    static class MainWorker extends Thread {
        @Override
        public void run() {
            try {
                XStream xs = new XStream();
                File confFile = new File("conf.xml");
                xs.alias("Configurations",Configurations.class);
                xs.alias("FTPConfiguration",FTPConfiguration.class);
                xs.alias("Table",Table.class);
                xs.addImplicitCollection(Configurations.class,"configurations","FTPConfiguration",FTPConfiguration.class);
                xs.addImplicitCollection(FTPConfiguration.class,"tables","Table",Table.class);	
                xs.useAttributeFor(Table.class,"lastMod");		
                xs.useAttributeFor(Table.class,"file");					
                Configurations confXML = (Configurations)xs.fromXML(new FileInputStream(confFile));
                Worker[] workers = new Worker[confXML.configurations.length];
                for(int i = 0; i < confXML.configurations.length; ++i) {
                    workers[i] =  new Worker(confXML.configurations[i]);
                    workers[i].start();
                }
                for(int i = 0; i < workers.length; ++i) workers[i].join();
                System.out.println("[MAIN] Writing down timestamps..");
                FileOutputStream fos = new FileOutputStream("conf.xml");
                String xmlOut = xs.toXML(confXML);
                byte[] bytes = xmlOut.getBytes("UTF-8");
                fos.write(bytes);
        } catch(Exception e) {
                e.printStackTrace();
        }
        System.out.println("[MAIN] Done");
        }
    }

    class Configurations {
        FTPConfiguration[] configurations;
    }

    class Table {
        String lastMod;
        String file;
    }

    class FTPConfiguration {
        Table[] tables;
        String host;
        String username;
        String password;
        int port;
    }

    static class Worker extends Thread {
        FTPConfiguration conf;
        Worker(FTPConfiguration c) {
            conf = c;
        }

        @Override
        public void run() {
            FTPWorker[] workers = new FTPWorker[conf.tables.length];
            for(int i = 0; i < conf.tables.length; ++i) {
                    workers[i] = new FTPWorker(conf.host,conf.username,conf.password,conf.port,conf.tables[i]);
                    workers[i].start();
            }

            for(int i = 0; i < workers.length; ++i) {
                    try{
                            workers[i].join();
                    } catch(Exception e) {
                            e.printStackTrace();
                    }
            }
        }
    }

    static class FTPWorker extends Thread {
        String 	host;
        String 	user;
        String 	pass;
        int 	port;
        Table  	table;

        FTPWorker(String h,String u,String p,int po,Table t) {
                host=h; user=u; pass=p; port=po; table=t;
        }

        void log(String msg) {
                System.out.println("[WORKER "+table.file+"] "+msg);
        }

        Date getModificationTime(String time) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                try {
        Date modificationTime = dateFormat.parse(time);
        return modificationTime;
        } catch (Exception ex) {
                //ex.printStackTrace();
                return new Date(Long.MIN_VALUE);
        }
        }

        String getDateString(Date d) {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                return df.format(d);
        }

        @Override
        public void run() {
            log("Started");
            try {
                FTPClient ftpc = new FTPClient();
                ftpc.connect(host);
                ftpc.login(user,pass);
                int reply = ftpc.getReplyCode();
                log("Got: "+Integer.toString(reply));
                String lastMod = ftpc.getModificationTime(table.file);
                Date remoteLast = getModificationTime(lastMod);
                Date localLast = getModificationTime(table.lastMod);
                //log("Remote date: "	+getDateString(remoteLast));
                //log("Local date: "	+getDateString(localLast));
                if(remoteLast.after(localLast)) {
                    log("Remote file changed");
                    log("Downloading file");
                    ftpc.retrieveFile(table.file,new FileOutputStream("../"+table.file));
                    table.lastMod = lastMod;
                } else {
                    log("Local file up to date");					
                }
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}