/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package filesearch;

/**
 *
 * @author darshu
 */
import java.io.File;
import java.time.Clock;
import java.time.ZoneId;
import java.util.*;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

class DiscoverFile extends Thread {

//	static int count = 0;
//	static int folderCount = 0;
//	
//	public static int printDirectory(File fp , int level)
//	{
//		
//		int ans = 0;
//		File[] fpList = fp.listFiles();
//		
//		if(fpList == null)
//		{
//			return 0;
//		}
//		
//		for(File obj : fpList)
//		{
//			if(obj == null)
//			{
//				return 1;
//			}
//			
//
//			count++;
//			System.out.println(count + " " +obj);
//			
//			if(obj.isDirectory() && !obj.isHidden())
//			{
//				folderCount++;
//				ans += printDirectory(obj , level+1);
//			}
//			
//			
//		}
//		return ans;
//	}
//	
//	static ArrayList<String> result = new ArrayList<String>();
//	
//	public static ArrayList<String> findFile(File fp , String toFind)
//	{
//		
//		File[] fpList = fp.listFiles();
//		
//		if(fpList == null)
//		{
//			return null;
//		}
//		
//		for(File obj : fpList)
//		{
//			if(obj.getName().contains(toFind))
//			{
//				result.add(obj.toString());
//			}
//			
//			if(obj.isDirectory() && !obj.isHidden())
//			{
//				findFile(obj, toFind);
//			}
//		}
//		
//		return result;
//	}
    Queue<File> searchQueue;
    File fp;

    public DiscoverFile(Queue<File> searchQueue, File fp) {

        this.searchQueue = searchQueue;
        this.fp = fp;
    }

    public void run() {

        System.out.println(this.getName() + " Producer Started");
        addToQueue();

        searchQueue.add(null);

        synchronized (searchQueue) {
            searchQueue.notifyAll();
        }

        System.out.println(this.getName() + " Producer Ended");
    }

    public void addToQueue() {
        File[] fpList = fp.listFiles();

        if (fpList == null) {
            return;
        }

        for (File file : fpList) {

            synchronized (searchQueue) {
                searchQueue.add(file);
                searchQueue.notifyAll();
            }

            if (file.isDirectory() && !file.isHidden()) {
                fp = file;
                addToQueue();
            }
        }
    }
}

class AddText implements Runnable{
    
    JTextArea res;
    String toAdd;

    public AddText(JTextArea res , String toAdd) {
        this.res = res;
        this.toAdd = toAdd;
    }    

    @Override
    public void run() {
        synchronized (res) {
            res.append(toAdd);
        }
        
    }
    
}

class FileValidate extends Thread {

    JTextArea res;

    Queue<File> searchQueue;

    ArrayList<String> result;

    String toSearch;

    public FileValidate(Queue<File> searchQueue, ArrayList<String> result, String toSearch, JTextArea res) {

        this.searchQueue = searchQueue;
        this.result = result;
        this.toSearch = toSearch;
        this.res = res;
    }

    @Override
    public void run() {
        System.out.println(this.getName() + " Consumer Started");
        validateFile(searchQueue, result, toSearch);
        System.out.println(this.getName() + " Consumer Ended");
    }

    public void validateFile(Queue<File> searchQueue, ArrayList<String> result, String toSearch) {
        while (true) {
            File fp = null;
            synchronized (searchQueue) {
                if (!searchQueue.isEmpty() && searchQueue.peek() != null) {
                    fp = searchQueue.remove();
                }
            }

            if (fp != null && fp.getName().contains(toSearch)) {
                res.append(fp.toString() + "\n");
            }

            if (searchQueue.isEmpty()) {
                try {
                    synchronized (searchQueue) {
                        searchQueue.wait();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            synchronized (searchQueue) {
                if (searchQueue.peek() == null && !searchQueue.isEmpty()) {
                    return;
                }
            }
        }
    }
}

class FileSearch {

    public static void searchFile(String fileName, String destination , JTextArea res) {

        File fp = new File(destination);

        Queue<File> searchQueue = new LinkedList<File>();

        ArrayList<String> result = new ArrayList<String>();

        DiscoverFile discoverer = new DiscoverFile(searchQueue, fp);

        discoverer.start();

        FileValidate validator1 = new FileValidate(searchQueue, result, fileName, res);
        FileValidate validator2 = new FileValidate(searchQueue, result, fileName, res);

        validator1.start();
        validator2.start();

        try {
            discoverer.join();
            validator1.join();
            validator2.join();
            //System.out.println(Clock.system(ZoneId.of("Asia/Calcutta")).instant());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
