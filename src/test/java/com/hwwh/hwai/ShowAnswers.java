package com.hwwh.hwai;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class ShowAnswers {

    @Autowired
    private ShowAnswers showAnswers;

    @Test
    public void test(){
        ShowAnswers showAnswers = this.showAnswers.showAnswers;
        System.out.println("文件修改成功!");
    }
}
