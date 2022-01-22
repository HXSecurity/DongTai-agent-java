package com.secnium.iast.core.report;

import org.junit.Test;

public class ApiReport {

    @Test
    public void a(){
        String anno = "interface org.springframework.web.bind.annotation.RequestParam";
        anno = anno.substring(anno.lastIndexOf(".")+1,anno.length());
        System.out.println(anno);
    }
}
