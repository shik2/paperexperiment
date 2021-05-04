package com.shik2.utils;

/**
 * @author shik2
 * @date 2020/12/18
 * <p>
 * Description:
 **/
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class FileHelper {
    private String fileName;

    public FileHelper(){

    }

    public FileHelper(String fileName){
        this.fileName=fileName;
    }

    public <T> void saveObjToFile(List<T> list){
        try {
            //写对象流的对象
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(fileName));

            oos.writeObject(list);                 //将Person对象p写入到oos中

            oos.close();                        //关闭文件流
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * 从文件中读出对象
     */
    public <T> List<T> getObjFromFile(){
        try {
            ObjectInputStream ois=new ObjectInputStream(new FileInputStream(fileName));
            List<T> list=(List<T>)ois.readObject();              //读出对象
            return list;                                       //返回对象
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
