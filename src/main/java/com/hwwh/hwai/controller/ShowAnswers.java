package com.hwwh.hwai.controller;

import org.apache.pdfbox.debugger.ui.Tree;
import org.jetbrains.annotations.TestOnly;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.*;

@RestController
@RequestMapping("/showAnswers")
public class ShowAnswers {


        @PostMapping("/showAnswersAction")
        public String ShowAnswers() throws IOException {
            String pptxPath = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\1 - 副本.pptx";
            String extractPath = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件";
            String extractPathXml = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件\\ppt\\slides";


            unzip(pptxPath, extractPath);
            // TODO: 解析、修改和保存解压缩文件夹中的 XML 文件
            ModifyXML(extractPathXml);
            Path sourceDir = Paths.get("C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件");
            Path zipFile = Paths.get("C:\\Users\\Q\\Desktop\\生成的文件汇总\\压缩生成的ppt文件\\1.pptx");
            zipDirectory(sourceDir,zipFile);
            return sourceDir.getName(0)+zipFile.getName(0).toString();
        }

        // 使用 Java 内置的 Zip 实用程序解压缩 pptx 文件
        public void unzip(String fileZip, String destDir) {
            File dir = new File(destDir);

            // 如果输出目录不存在，则创建输出目录
            if(!dir.exists()) dir.mkdirs();

            byte[] buffer = new byte[1024];

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))){
                ZipEntry zipEntry = zis.getNextEntry();

                while(zipEntry != null){
                    File newFile = newFile(dir, zipEntry);
                    if(zipEntry.isDirectory()){
                        newFile.mkdirs();
                    }else{
                        newFile.getParentFile().mkdirs();
                        try(FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    zipEntry = zis.getNextEntry();
                }

                zis.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 使用 Java 内置的 Zip 实用程序重新压缩文件夹
        public void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
            try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
                Path pp = Paths.get(sourceDir.toString());
                Files.walk(pp)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                            try {
                                zipStream.putNextEntry(zipEntry);
                                Files.copy(path, zipStream);
                                zipStream.closeEntry();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }

        // Function to prevent Zip Slip Vulnerability
        public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
            File destFile = new File(destinationDir, zipEntry.getName());
            String destDirPath = destinationDir.getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();

            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
            }

            return destFile;
        }


   /* public static void ModifyXML(String directoryPath) {

        // 获取文件夹中的所有.xml文件
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        try {
            for (File file : listOfFiles) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);

                // 获取节点
                Node node = doc.getElementsByTagName("targetElementTag").item(0);

                // 更新节点值
                node.setTextContent("new value");

                // 写回xml文件
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("path/to/new/xml/directory/" + file.getName()));
                transformer.transform(source, result);

                System.out.println(file.getName() + " Done");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    /*public void ModifyXML(String directoryPath) {
        // 获取文件夹中的所有.xml文件
        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

        try {
            for (File file : listOfFiles) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(true); // VERY IMPORTANT
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);

                XPathFactory xpathfactory = XPathFactory.newInstance();
                XPath xpath = xpathfactory.newXPath();
                xpath.setNamespaceContext(new MyNamespaceContext());
                XPathExpression expr = xpath.compile("//a:solidFill[a:srgbClr/@val='FF0000']/ancestor::p:sp/descendant::a:t");

                NodeList redTextElements = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                for (int i = 0; i < redTextElements.getLength(); i++) {
                    Node redTextElement = redTextElements.item(i);
                    Node sp = redTextElement.getParentNode().getParentNode();

                    Element transition = doc.createElementNS("http://schemas.openxmlformats.org/presentationml/2006/main", "p:transition");
                    transition.setAttribute("transition", "fly");

                    if (sp.getNodeName().equals("p:sp")) {
                        NodeList transitions = ((Element) sp).getElementsByTagName("p:transition");
                        for (int j = 0; j < transitions.getLength(); j++) {
                            sp.removeChild(transitions.item(j));
                        }
                        sp.appendChild(transition);
                    }
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件" + file.getName()));
                transformer.transform(source, result);

                System.out.println(file.getName() + " Done");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
   public void ModifyXML(String directoryPath) {
       // 获取文件夹中的所有.xml文件
       File folder = new File(directoryPath);
       File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));

       try {
           for (File file : listOfFiles) {
               int x = 1;
               DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
               docFactory.setNamespaceAware(true); // VERY IMPORTANT
               DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
               Document doc = docBuilder.parse(file);

               XPathFactory xpathfactory = XPathFactory.newInstance();
               XPath xpath = xpathfactory.newXPath();
               // 需要导入命名空间
               xpath.setNamespaceContext(new MyNamespaceContext());
               XPathExpression expr = xpath.compile("//a:solidFill[a:srgbClr/@val='FF0000']/parent::a:rPr/parent::a:r/a:t");
               //XPathExpression textSpid = xpath.compile("//p:sp[a:txBody/a:p/a:r/a:rPr/a:solidFill/a:srgbClr/@val='FF0000']/p:nvSpPr/p:cNvPr/@id");
               XPathExpression textSpid = xpath.compile("../../p:nvSpPr/p:cNvPr/@id");

               NodeList redTextElements = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

               String pNamespace = "http://schemas.openxmlformats.org/presentationml/2006/main";
               String aNamespace = "http://schemas.openxmlformats.org/drawingml/2006/main";
               Element sld = (Element)doc.getElementsByTagName("p:sld").item(0);
               Element timing = doc.createElementNS(pNamespace,"p:timing");
               sld.appendChild(timing);
               Element tnLst = doc.createElementNS(pNamespace,"p:tnLst");
               timing.appendChild(tnLst);
               Element parTop = doc.createElementNS(pNamespace,"p:par");
               tnLst.appendChild(parTop);
               Element cTn = doc.createElementNS(pNamespace,"p:cTn");
               cTn.setAttribute("id",String.valueOf(x++));
               cTn.setAttribute("dur","indefinite");
               cTn.setAttribute("restart","never");
               cTn.setAttribute("nodeType","tmRoot");
               parTop.appendChild(cTn);

               Element childTnLstTop = doc.createElementNS(pNamespace, "p:childTnLst");
               cTn.appendChild(childTnLstTop);

               Element seq = doc.createElementNS(pNamespace, "p:seq");
               seq.setAttribute("concurrent", "1");
               seq.setAttribute("nextAc", "seek");
               childTnLstTop.appendChild(seq);

               Element cTnSeq = doc.createElementNS(pNamespace, "p:cTn");
               cTnSeq.setAttribute("id", String.valueOf(x++));
               cTnSeq.setAttribute("dur", "indefinite");
               cTnSeq.setAttribute("nodeType", "mainSeq");
               seq.appendChild(cTnSeq);

               Element childTnLstSeq = doc.createElementNS(pNamespace, "p:childTnLst");
               cTnSeq.appendChild(childTnLstSeq);

               TreeMap<String, ArrayList<Node>> spidMap = new TreeMap<>();

               for (int i = 0; i < redTextElements.getLength(); i++) {
                   System.out.println(redTextElements.item(i).getTextContent());
                   Node redTextElement = redTextElements.item(i);
                   Node sp = redTextElement.getParentNode().getParentNode();

                   Node spIdNode = (Node) textSpid.evaluate(sp, XPathConstants.NODE);
                   String currentSpid = spIdNode == null ? "N/A" : spIdNode.getTextContent();
                   System.out.println(currentSpid);


                   if (sp.getNodeName().equals("a:p")) {
                       if (!spidMap.containsKey(currentSpid) && !redTextElement.getTextContent().equals("♝")) {
                           spidMap.put(currentSpid, new ArrayList<Node>());
                       }
                       if(!redTextElement.getTextContent().equals("♝"))spidMap.get(currentSpid).add(redTextElement);
                   }
               }
               for (Map.Entry<String, ArrayList<Node>> entry : spidMap.entrySet()) {
                   int length = entry.getValue().size();
                   String currentSpid = entry.getKey();
                       Element parSeq = doc.createElementNS(pNamespace, "p:par");
                       childTnLstSeq.appendChild(parSeq);

                       Element cTnParSeq = doc.createElementNS(pNamespace, "p:cTn");
                       cTnParSeq.setAttribute("id", String.valueOf(x++));
                       cTnParSeq.setAttribute("fill", "hold");
                       parSeq.appendChild(cTnParSeq);

                       Element stCondLst = doc.createElementNS(pNamespace, "p:stCondLst");
                       cTnParSeq.appendChild(stCondLst);

                       Element cond = doc.createElementNS(pNamespace, "p:cond");
                       cond.setAttribute("delay", "indefinite");
                       stCondLst.appendChild(cond);


                       Element childTnLst = doc.createElementNS(pNamespace, "p:childTnLst");
                       cTnParSeq.appendChild(childTnLst);
                       Element par = doc.createElementNS(pNamespace, "p:par");
                       childTnLst.appendChild(par);

                       Element cTnFirstChildTnLst = doc.createElementNS(pNamespace, "p:cTn");
                       cTnFirstChildTnLst.setAttribute("id", String.valueOf(x++));
                       cTnFirstChildTnLst.setAttribute("fill", "hold");
                       par.appendChild(cTnFirstChildTnLst);

                       Element stCondLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:stCondLst");
                       cTnFirstChildTnLst.appendChild(stCondLstFirstChildTnLst);

                       Element condFirstChildTnLst = doc.createElementNS(pNamespace, "p:cond");
                       condFirstChildTnLst.setAttribute("delay", "0");
                       stCondLstFirstChildTnLst.appendChild(condFirstChildTnLst);


                       Element childTnLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:childTnLst");
                       cTnFirstChildTnLst.appendChild(childTnLstFirstChildTnLst);
                     //int a= 0;
                     for(int a = 0;a<length;a++){
//                   for (Node element : entry.getValue()) {

                           Element parChildTnLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:par");
                           childTnLstFirstChildTnLst.appendChild(parChildTnLstFirstChildTnLst);

                           Element cTnChildTnLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:cTn");
                           cTnChildTnLstFirstChildTnLst.setAttribute("id", String.valueOf(x++));
                           cTnChildTnLstFirstChildTnLst.setAttribute("presetID", "2");
                           cTnChildTnLstFirstChildTnLst.setAttribute("presetClass", "entr");
                           cTnChildTnLstFirstChildTnLst.setAttribute("presetSubtype", "8");
                           cTnChildTnLstFirstChildTnLst.setAttribute("fill", "hold");
                           cTnChildTnLstFirstChildTnLst.setAttribute("nodeType", "clickEffect");
                           parChildTnLstFirstChildTnLst.appendChild(cTnChildTnLstFirstChildTnLst);

                           Element stCondLstChildTnLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:stCondLst");
                           cTnChildTnLstFirstChildTnLst.appendChild(stCondLstChildTnLstFirstChildTnLst);

                           Element condChildTnLstFirstChildTnLst = doc.createElementNS(pNamespace, "p:cond");
                           condChildTnLstFirstChildTnLst.setAttribute("delay", "0");
                           stCondLstChildTnLstFirstChildTnLst.appendChild(condChildTnLstFirstChildTnLst);

                           Element cTnCHildTnLstFirstChildTnLstChild = doc.createElementNS(pNamespace, "p:childTnLst");
                           cTnChildTnLstFirstChildTnLst.appendChild(cTnCHildTnLstFirstChildTnLstChild);
                           Element set = doc.createElementNS(pNamespace, "p:set");
                           cTnCHildTnLstFirstChildTnLstChild.appendChild(set);

                           Element cBhvr = doc.createElementNS(pNamespace, "p:cBhvr");
                           set.appendChild(cBhvr);

                           Element cTnSet = doc.createElementNS(pNamespace, "p:cTn");
                           cTnSet.setAttribute("id", String.valueOf(x++));
                           cTnSet.setAttribute("dur", "1");
                           cTnSet.setAttribute("fill", "hold");
                           cBhvr.appendChild(cTnSet);


                           Element stCondLstSet = doc.createElementNS(pNamespace, "p:stCondLst");
                           cTnSet.appendChild(stCondLstSet);

                           Element condSet = doc.createElementNS(pNamespace, "p:cond");
                           condSet.setAttribute("delay", "0");
                           stCondLstSet.appendChild(condSet);

                           Element tgtEl = doc.createElementNS(pNamespace, "p:tgtEl");
                           cBhvr.appendChild(tgtEl);
                           Element spTgt = doc.createElementNS(pNamespace, "p:spTgt");
                           spTgt.setAttribute("spid", currentSpid);
                           tgtEl.appendChild(spTgt);
                           Element txEl = doc.createElementNS(pNamespace, "p:txEl");
                           spTgt.appendChild(txEl);
                           Element pRg = doc.createElementNS(pNamespace, "p:pRg");
                           pRg.setAttribute("st", String.valueOf(a));
                           pRg.setAttribute("end", String.valueOf(a));
                           txEl.appendChild(pRg);
                           Element attrNameLst = doc.createElementNS(pNamespace, "p:attrNameLst");
                           cBhvr.appendChild(attrNameLst);
                           Element attrName = doc.createElementNS(pNamespace, "p:attrName");
                           attrName.setTextContent("style.visibility");
                           attrNameLst.appendChild(attrName);


                           Element to = doc.createElementNS(pNamespace, "p:to");
                           set.appendChild(to);

                           Element strVal = doc.createElementNS(pNamespace, "p:strVal");
                           strVal.setAttribute("val", "visible");
                           to.appendChild(strVal);

                           Element anim = doc.createElementNS(pNamespace, "p:anim");
                           anim.setAttribute("calcmode", "lin");
                           anim.setAttribute("valueType", "num");
                           cTnCHildTnLstFirstChildTnLstChild.appendChild(anim);
                           Element animcBhvr = doc.createElementNS(pNamespace, "p:cBhvr");
                           animcBhvr.setAttribute("additive", "base");
                           anim.appendChild(animcBhvr);
                           Element animcBhvrcTn = doc.createElementNS(pNamespace, "p:cTn");
                           animcBhvrcTn.setAttribute("id", String.valueOf(x++));
                           animcBhvrcTn.setAttribute("dur", "500");
                           animcBhvrcTn.setAttribute("fill", "hold");
                           animcBhvr.appendChild(animcBhvrcTn);
                           Element animcBhvrtgtEl = doc.createElementNS(pNamespace, "p:tgtEl");
                           animcBhvr.appendChild(animcBhvrtgtEl);
                           Element animcBhvrtgtElspTgt = doc.createElementNS(pNamespace, "p:spTgt");
                           animcBhvrtgtElspTgt.setAttribute("spid", currentSpid);
                           animcBhvrtgtEl.appendChild(animcBhvrtgtElspTgt);
                           Element animcBhvrtgtElspTgttxEl = doc.createElementNS(pNamespace, "p:txEl");
                           animcBhvrtgtElspTgt.appendChild(animcBhvrtgtElspTgttxEl);
                           Element animcBhvrtgtElspTgttxElpRg = doc.createElementNS(pNamespace, "p:pRg");
                           animcBhvrtgtElspTgttxElpRg.setAttribute("st", String.valueOf(a));
                           animcBhvrtgtElspTgttxElpRg.setAttribute("end", String.valueOf(a));
                           animcBhvrtgtElspTgttxEl.appendChild(animcBhvrtgtElspTgttxElpRg);
                           Element animcBhvrattrNameLst = doc.createElementNS(pNamespace, "p:attrNameLst");
                           Element animcBhvrattrNameLstattrName = doc.createElementNS(pNamespace, "p:attrName");
                           animcBhvrattrNameLstattrName.setTextContent("ppt_x");
                           animcBhvrattrNameLst.appendChild(animcBhvrattrNameLstattrName);
                           animcBhvr.appendChild(animcBhvrattrNameLst);
                           Element animtavLst = doc.createElementNS(pNamespace, "p:tavLst");
                           anim.appendChild(animtavLst);
                           Element animtavLsttav = doc.createElementNS(pNamespace, "p:tav");
                           animtavLsttav.setAttribute("tm", "0");
                           animtavLst.appendChild(animtavLsttav);
                           Element animtavLsttavval = doc.createElementNS(pNamespace, "p:val");
                           animtavLsttav.appendChild(animtavLsttavval);
                           Element animtavLsttavvalstrVal = doc.createElementNS(pNamespace, "p:strVal");
                           animtavLsttavvalstrVal.setAttribute("val", "0-#ppt_w/2");
                           animtavLsttavval.appendChild(animtavLsttavvalstrVal);
                           Element animtavLsttavtwo = doc.createElementNS(pNamespace, "p:tav");
                           animtavLsttavtwo.setAttribute("tm", "100000");
                           animtavLst.appendChild(animtavLsttavtwo);
                           Element animtavLsttavtwoval = doc.createElementNS(pNamespace, "p:val");
                           animtavLsttavtwo.appendChild(animtavLsttavtwoval);
                           Element animtavLsttavtwovalstrVal = doc.createElementNS(pNamespace, "p:strVal");
                           animtavLsttavtwovalstrVal.setAttribute("val", "ppt_x");
                           animtavLsttavtwoval.appendChild(animtavLsttavtwovalstrVal);

                           Element animtwo = doc.createElementNS(pNamespace, "p:anim");
                           animtwo.setAttribute("calcmode", "lin");
                           animtwo.setAttribute("valueType", "num");
                           cTnCHildTnLstFirstChildTnLstChild.appendChild(animtwo);
                           Element animtwocBhvr = doc.createElementNS(pNamespace, "p:cBhvr");
                           animtwocBhvr.setAttribute("additive", "base");
                           animtwo.appendChild(animcBhvr);
                           Element animtwocBhvrcTn = doc.createElementNS(pNamespace, "p:cTn");
                           animtwocBhvrcTn.setAttribute("id", String.valueOf(x++));
                           animtwocBhvrcTn.setAttribute("dur", "500");
                           animtwocBhvrcTn.setAttribute("fill", "hold");
                           animtwocBhvr.appendChild(animcBhvrcTn);
                           Element animtwocBhvrtgtEl = doc.createElementNS(pNamespace, "p:tgtEl");
                           animtwocBhvr.appendChild(animtwocBhvrtgtEl);
                           Element animtwocBhvrtgtElspTgt = doc.createElementNS(pNamespace, "p:spTgt");
                           animtwocBhvrtgtElspTgt.setAttribute("spid", currentSpid);
                           animtwocBhvrtgtEl.appendChild(animtwocBhvrtgtElspTgt);
                           Element animtwocBhvrtgtElspTgttxEl = doc.createElementNS(pNamespace, "p:txEl");
                           animtwocBhvrtgtElspTgt.appendChild(animtwocBhvrtgtElspTgttxEl);
                           Element animtwocBhvrtgtElspTgttxElpRg = doc.createElementNS(pNamespace, "p:pRg");
                           animtwocBhvrtgtElspTgttxElpRg.setAttribute("st", String.valueOf(a));
                           animtwocBhvrtgtElspTgttxElpRg.setAttribute("end", String.valueOf(a));
                           animtwocBhvrtgtElspTgttxEl.appendChild(animtwocBhvrtgtElspTgttxElpRg);
                           Element animtwocBhvrattrNameLst = doc.createElementNS(pNamespace, "p:attrNameLst");
                           animtwocBhvr.appendChild(animtwocBhvrattrNameLst);
                           Element animtwocBhvrattrNameLstattrName = doc.createElementNS(pNamespace, "p:attrName");
                           animtwocBhvrattrNameLstattrName.setTextContent("ppt_y");
                           animtwocBhvrattrNameLst.appendChild(animcBhvrattrNameLstattrName);
                           Element animtwotavLst = doc.createElementNS(pNamespace, "p:tavLst");
                           animtwo.appendChild(animtwotavLst);
                           Element animtwotavLsttav = doc.createElementNS(pNamespace, "p:tav");
                           animtwotavLsttav.setAttribute("tm", "0");
                           animtwotavLst.appendChild(animtwotavLsttav);
                           Element animtwotavLsttavval = doc.createElementNS(pNamespace, "p:val");
                           animtwotavLsttav.appendChild(animtwotavLsttavval);
                           Element animtwotavLsttavvalstrVal = doc.createElementNS(pNamespace, "p:strVal");
                           animtwotavLsttavvalstrVal.setAttribute("val", "0-#ppt_y");
                           animtwotavLsttavval.appendChild(animtwotavLsttavvalstrVal);
                           Element animtwotavLsttavtwo = doc.createElementNS(pNamespace, "p:tav");
                           animtwotavLsttavtwo.setAttribute("tm", "100000");
                           animtwotavLst.appendChild(animtwotavLsttavtwo);
                           Element animtwotavLsttavtwoval = doc.createElementNS(pNamespace, "p:val");
                           animtwotavLsttavtwo.appendChild(animtwotavLsttavtwoval);
                           Element animtwotavLsttavtwovalstrVal = doc.createElementNS(pNamespace, "p:strVal");
                           animtwotavLsttavtwovalstrVal.setAttribute("val", "ppt_y");
                           animtwotavLsttavtwoval.appendChild(animtwotavLsttavtwovalstrVal);
                           //a++;
//
                   }
               }
               Element prevCondLst = doc.createElementNS(pNamespace,"p:prevCondLst");
               seq.appendChild(prevCondLst);

               Element prevCondLstcond = doc.createElementNS(pNamespace,"p:cond");
               prevCondLstcond.setAttribute("evt","onPrve");
               prevCondLstcond.setAttribute("delay","0");
               prevCondLst.appendChild(prevCondLstcond);

               Element prevCondLstcondtgtEl = doc.createElementNS(pNamespace,"p:tgtEl");
               prevCondLstcond.appendChild(prevCondLstcondtgtEl);

               Element prevCondLstcondtgtElsldTgt = doc.createElementNS(pNamespace,"p:sldTgt");
               prevCondLstcondtgtEl.appendChild(prevCondLstcondtgtElsldTgt);

               Element nextCondLst = doc.createElementNS(pNamespace,"p:nextCondLst");
               seq.appendChild(nextCondLst);

               Element nextCondLstcond = doc.createElementNS(pNamespace,"p:cond");
               nextCondLstcond.setAttribute("evt","onNext");
               nextCondLstcond.setAttribute("delay","0");
               nextCondLst.appendChild(nextCondLstcond);

               Element nextCondLstcondtgtEl =doc.createElementNS(pNamespace,"p:tgtEl");
               nextCondLstcond.appendChild(nextCondLstcondtgtEl);

               Element nextCondLstcondtgtElsldTgt = doc.createElementNS(pNamespace,"p:sldTgt");
               nextCondLstcondtgtEl.appendChild(nextCondLstcondtgtElsldTgt);

               TransformerFactory transformerFactory = TransformerFactory.newInstance();
               Transformer transformer = transformerFactory.newTransformer();
               DOMSource source = new DOMSource(doc);
               StreamResult result = new StreamResult(new File("C:\\Users\\Q\\Desktop\\生成的文件汇总\\" + file.getName()));
               transformer.transform(source, result);

               System.out.println(file.getName() + " Done");
               File oldFile = new File("C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件\\ppt\\slides\\"+file.getName());
               File newFile = new File("C:\\Users\\Q\\Desktop\\生成的文件汇总\\"+file.getName());
               if(oldFile.delete()){
                   newFile.renameTo(oldFile);
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }


    public class MyNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) throw new NullPointerException("Null prefix");
            else if ("a".equals(prefix)) return "http://schemas.openxmlformats.org/drawingml/2006/main";
            else if ("p".equals(prefix)) return "http://schemas.openxmlformats.org/presentationml/2006/main";
            else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }
}
