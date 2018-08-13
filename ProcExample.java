package com.kinetica;

import java.util.Map ;
import java.util.HashMap ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Calendar ;
import java.util.GregorianCalendar ;
import java.lang.Long ;
import java.lang.Float ;
import java.lang.System.*;

//dfxPoolQuoteからTOBのx msec足を作成する
public class ProcExample {
    static final int MSEC_PER_ONE_DAY = 86400000 ;
    public static void main(String[] args) {
        ProcData procData = ProcData.get();
        Map<String,String> paramsMap = procData.getParams() ;
        Map<String,String> resultMap = new HashMap() ;
        
        //x msec足かを持つ変数
        int chartUnit = (int)Long.parseLong(paramsMap.get("chartUnit")) ;        
            
        //入出力テーブルの取得
        ProcData.InputTable inputTable = procData.getInputData().getTable(0) ;
        ProcData.OutputTable outputTable = procData.getOutputData().getTable(0) ;

        //unitに応じてoutput tableのサイズ確保
        outputTable.setSize(MSEC_PER_ONE_DAY/chartUnit) ;
        
        //入力カラムの取得
        ProcData.InputColumn inputColumn2 = inputTable.getColumn("col2") ;
        ProcData.InputColumn inputColumn3 = inputTable.getColumn("col3_hr_min") ;
        ProcData.InputColumn inputColumn7 = inputTable.getColumn("col7") ;
        ProcData.InputColumn inputColumn12 = inputTable.getColumn("col12") ;
        ProcData.InputColumn inputColumn15 = inputTable.getColumn("col15") ;
        ProcData.InputColumn inputColumn16 = inputTable.getColumn("col16") ;
        ProcData.InputColumn inputColumn26 = inputTable.getColumn("col26") ;
        ProcData.InputColumn inputColumn27 = inputTable.getColumn("col27") ;
        
        //出力カラムの設定
        ProcData.OutputColumn outputColumn3 = outputTable.getColumn(0);
        ProcData.OutputColumn outputColumn15First = outputTable.getColumn(1) ;
        ProcData.OutputColumn outputColumn15Max = outputTable.getColumn(2) ;
        ProcData.OutputColumn outputColumn15Min = outputTable.getColumn(3) ;
        ProcData.OutputColumn outputColumn15Last = outputTable.getColumn(4) ;
        ProcData.OutputColumn outputColumn15Median = outputTable.getColumn(5) ;
        ProcData.OutputColumn outputColumn16First = outputTable.getColumn(6) ;
        ProcData.OutputColumn outputColumn16Max = outputTable.getColumn(7) ;
        ProcData.OutputColumn outputColumn16Min = outputTable.getColumn(8) ;
        ProcData.OutputColumn outputColumn16Last = outputTable.getColumn(9) ;
        ProcData.OutputColumn outputColumn16Median = outputTable.getColumn(10) ;

        //出力用一時領域
        int outputTableSize = MSEC_PER_ONE_DAY/chartUnit ;
        
        //column15の各値用の一時格納領域
        float[] tempOutputColumn15First = new float[outputTableSize] ;
        float[] tempOutputColumn15Max = new float[outputTableSize] ;
        float[] tempOutputColumn15Min = new float[outputTableSize] ;
        float[] tempOutputColumn15Last = new float[outputTableSize] ;
        //first,last用に現状のcolumn15値に対応するcolumn3の値を持つ一時格納領域
        long[] tempOutputColumn15FirstColumn3 = new long[outputTableSize] ;
        long[] tempOutputColumn15LastColumn3 = new long[outputTableSize] ;
        
        //column15の各値用の一時格納領域
        float[] tempOutputColumn16First = new float[outputTableSize] ;
        float[] tempOutputColumn16Max = new float[outputTableSize] ;
        float[] tempOutputColumn16Min = new float[outputTableSize] ;
        float[] tempOutputColumn16Last = new float[outputTableSize] ;
        //first,last用に現状のcolumn15値に対応するcolumn3の値を持つ一時格納領域
        long[] tempOutputColumn16FirstColumn3 = new long[outputTableSize] ;
        long[] tempOutputColumn16LastColumn3 = new long[outputTableSize] ;
        
        //中央値用の値格納領域
        List<Float>[] column15Values = new ArrayList[outputTableSize] ;
        List<Float>[] column16Values = new ArrayList[outputTableSize] ;

        //1回以上処理済みのレコード(領域)かどうか
        boolean[] isTouchedRecord = new boolean[outputTableSize] ;
        
        System.out.println("outputTableSize:"+String.valueOf(outputTableSize)) ;
        System.out.println("inputTableSize:"+String.valueOf(inputTable.getSize())) ;
        
        //テーブル行数分繰り返し
        for (int i = 0; i < inputTable.getSize() ; i++) {            
            //処理外レコード判定
            
            if(!("USD/JPY".equals(inputColumn2.getChar(i))) || 
               !("TKYMIZ.Edo".equals(inputColumn7.getChar(i))) ||
               !("A".equals(inputColumn26.getChar(i))) || 
               !("A".equals(inputColumn27.getChar(i))) || 
               !("0".equals(inputColumn12.getChar(i))) ){
                continue ;
            }
            
            //output用のインデックスを計算 => col3 HH:mm:ss.msecの整数表現 ÷ chartUnit
            int index = (int)(inputColumn3.getCalendar(i).getTimeInMillis() % MSEC_PER_ONE_DAY / chartUnit) ;
            
            //最大値、最小値、始値、終値を判定しながら格納
            //First
            if(!isTouchedRecord[index] ||  inputColumn3.getCalendar(i).getTimeInMillis() < tempOutputColumn15FirstColumn3[index]){
                tempOutputColumn15First[index] = inputColumn15.getFloat(i) ;
                tempOutputColumn15FirstColumn3[index] = inputColumn3.getCalendar(i).getTimeInMillis() ; 
            }            
            if(!isTouchedRecord[index] ||  inputColumn3.getCalendar(i).getTimeInMillis() < tempOutputColumn16FirstColumn3[index]){
                tempOutputColumn16First[index] = inputColumn16.getFloat(i) ;
                tempOutputColumn16FirstColumn3[index] = inputColumn3.getCalendar(i).getTimeInMillis() ; 
            }            
            //Last
            if(inputColumn3.getCalendar(i).getTimeInMillis() > tempOutputColumn15LastColumn3[index]){
                tempOutputColumn15Last[index] = inputColumn15.getFloat(i) ;
                tempOutputColumn15LastColumn3[index] = inputColumn3.getCalendar(i).getTimeInMillis() ; 
            }
            if(inputColumn3.getCalendar(i).getTimeInMillis() > tempOutputColumn16LastColumn3[index]){
                tempOutputColumn16Last[index] = inputColumn16.getFloat(i) ;
                tempOutputColumn16LastColumn3[index] = inputColumn3.getCalendar(i).getTimeInMillis() ; 
            }
            //Min
            if(!isTouchedRecord[index] ||  inputColumn15.getFloat(i) < tempOutputColumn15Min[index]){
                tempOutputColumn15Min[index] = inputColumn15.getFloat(i) ;
            }            
            if(!isTouchedRecord[index] ||  inputColumn16.getFloat(i) < tempOutputColumn16Min[index]){
                tempOutputColumn16Min[index] = inputColumn16.getFloat(i) ;
            }            
            //Max
            if(inputColumn15.getFloat(i) > tempOutputColumn15Max[index]){
                tempOutputColumn15Max[index] = inputColumn15.getFloat(i) ;
            }
            if(inputColumn16.getFloat(i) > tempOutputColumn16Max[index]){
                tempOutputColumn16Max[index] = inputColumn16.getFloat(i) ;
            }

            //初回処理であれば、isTouchedRecordを切り替えとListの生成
            if(!isTouchedRecord[index]){
                isTouchedRecord[index] = true ;
                column15Values[index] = new ArrayList<Float>() ;
                column16Values[index] = new ArrayList<Float>() ;
            }

            
            //中央値用の配列にcolumn15とcolumn16を追加していく
            column15Values[index].add(inputColumn15.getFloat(i)) ;
            column16Values[index].add(inputColumn16.getFloat(i)) ;
            
            
        }

        int nullRecordCount = 0;
        //出力テーブル行数分繰り返し
        for (int i = 0; i < outputTableSize; i++){
            //未処理の時間帯はスキップ
            if(!isTouchedRecord[i]){
                nullRecordCount ++ ;
                continue ;
            }

            //時間帯の設定(一日未満の時間情報を削除後に、chartUnitでまとめた時間を足す)
            long column3 = tempOutputColumn15FirstColumn3[i] /  MSEC_PER_ONE_DAY * MSEC_PER_ONE_DAY + i * chartUnit ;
            Calendar calendar = new GregorianCalendar() ;
            calendar.setTimeInMillis(column3) ;
            outputColumn3.setCalendar(i,calendar) ;
            
            //各々の値をouputTablesに設定
            outputColumn15First.setFloat(i,new Float(tempOutputColumn15First[i])) ;
            outputColumn15Min.setFloat(i,new Float(tempOutputColumn15Min[i])) ;
            outputColumn15Max.setFloat(i,new Float(tempOutputColumn15Max[i])) ;
            outputColumn15Last.setFloat(i,new Float(tempOutputColumn15Last[i])) ;
            outputColumn16First.setFloat(i,new Float(tempOutputColumn16First[i])) ;
            outputColumn16Min.setFloat(i,new Float(tempOutputColumn16Min[i])) ;
            outputColumn16Max.setFloat(i,new Float(tempOutputColumn16Max[i])) ;
            outputColumn16Last.setFloat(i,new Float(tempOutputColumn16Last[i])) ;
            
            //column15のmedian設定
            Collections.sort(column15Values[i]) ;
            if(column15Values[i].size() % 2 == 0){
                float median = (column15Values[i].get(column15Values[i].size() / 2 -1) + column15Values[i].get(column15Values[i].size() / 2)) /2 ;
                outputColumn15Median.setFloat(i,new Float(median)) ;
            }else{
                float median = column15Values[i].get(column15Values[i].size() /2) ;
                outputColumn15Median.setFloat(i,new Float(median)) ;
            }
            
            //column16のmedian設定
            Collections.sort(column16Values[i]) ;
            if(column16Values[i].size() % 2 == 0){
                float median = (column16Values[i].get(column16Values[i].size() / 2 -1) + column16Values[i].get(column16Values[i].size() / 2)) /2 ;
                outputColumn16Median.setFloat(i,new Float(median)) ;
            }else{
                float median = column16Values[i].get(column16Values[i].size()/2) ;
                outputColumn16Median.setFloat(i,new Float(median)) ;
            }

            //処理済みの時間帯だが、nullRecordCountが1以上
            if(nullRecordCount > 0){
                int targetRange = nullRecordCount + 1 ;
                
                //線形補完した値を設定
                for (int j=i-nullRecordCount; j < i ; j++){
                    //補完対象のレコードのcolumn3設定
                    long nullColumn3 = tempOutputColumn15FirstColumn3[i] /  MSEC_PER_ONE_DAY * MSEC_PER_ONE_DAY + j * chartUnit ;
                    Calendar col3Calendar = new GregorianCalendar() ;
                    calendar.setTimeInMillis(nullColumn3) ;
                    outputColumn3.setCalendar(j,calendar) ;

                    //補完
                    outputColumn15First.setFloat(j,outputColumn15First.getFloat(i-targetRange) + (outputColumn15First.getFloat(i) - outputColumn15First.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn15Min.setFloat(j,outputColumn15Min.getFloat(i-targetRange) + (outputColumn15Min.getFloat(i) - outputColumn15Min.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn15Max.setFloat(j,outputColumn15Max.getFloat(i-targetRange) + (outputColumn15Max.getFloat(i) - outputColumn15Max.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn15Last.setFloat(j,outputColumn15Last.getFloat(i-targetRange) + (outputColumn15Last.getFloat(i) - outputColumn15Last.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;

                    outputColumn15Median.setFloat(j,outputColumn15Median.getFloat(i-targetRange) + (outputColumn15Median.getFloat(i) - outputColumn15Median.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;

                    outputColumn16First.setFloat(j,outputColumn16First.getFloat(i-targetRange) + (outputColumn16First.getFloat(i) - outputColumn16First.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn16Min.setFloat(j,outputColumn16Min.getFloat(i-targetRange) + (outputColumn16Min.getFloat(i) - outputColumn16Min.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn16Max.setFloat(j,outputColumn16Max.getFloat(i-targetRange) + (outputColumn16Max.getFloat(i) - outputColumn16Max.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                    
                    outputColumn16Last.setFloat(j,outputColumn16Last.getFloat(i-targetRange) + (outputColumn16Last.getFloat(i) - outputColumn16Last.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;

                    outputColumn16Median.setFloat(j,outputColumn16Median.getFloat(i-targetRange) + (outputColumn16Median.getFloat(i) - outputColumn16Median.getFloat(i-targetRange)) / targetRange * (targetRange - (i-j))) ;
                }
                
                nullRecordCount = 0;
            }            
            
        }
        
        procData.getResults().putAll(procData.getParams());
        procData.getBinResults().putAll(procData.getBinParams());
        procData.complete();
    }
}

