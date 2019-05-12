package application;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class VoiceInfo {
	String name;
	boolean isMarked;
	int LSBvalue;
	
	public VoiceInfo(String name, boolean isMarked){
		this.name = name;
		this.isMarked = isMarked;
	}
	public VoiceInfo(String name, String use, int LSBvalue){
		this.name = name;
		this.LSBvalue = LSBvalue;
		if(use!=null && use.equals("1")) {
			this.isMarked = true;
		} else {
			this.isMarked = false;
		}
	}
	public VoiceInfo(String name, String use){
		this.name = name;
		this.LSBvalue = 0;
		if(use!=null && use.equals("1")) {
			this.isMarked = true;
		} else {
			this.isMarked = false;
		}
	}
}

public class MapVoiceList {
	LinkedHashMap<String, VoiceInfo> mapVoiceList;
	String[][][] XGVoiceInfo = new String[16][8][40];
	String[] GroupNames = new String[16];
	private DocumentBuilderFactory factory;
	DocumentBuilder builder;
	private InputStream input;
	private Document dc;
	private XPathFactory xpf;
	private XPath xp;
	private XPathExpression xpe1;
	
	public MapVoiceList(){
		mapVoiceList = new LinkedHashMap<String, VoiceInfo>();
		parseXML();
		System.out.println("map size is:" + mapVoiceList.size());
	}
	
	public String getVoiceName(int voiceNo){
		String s = 	mapVoiceList.get(String.valueOf(voiceNo)).name;
		return s;
	}

	public String getGroupName(int groupNo){
		return GroupNames[groupNo];
	}
	public String getXGVoiceName(int groupNo, int PCNo, int LSBvalue){
		return XGVoiceInfo[groupNo][PCNo][LSBvalue];
	}
	public LinkedHashMap<String, String> getXGVoiceNames(int PCno){
		String strPCno = String.format("%3d",PCno);
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();

		try {
			xpe1 = xp.compile("/ModuleData/InstrumentList/Map[attribute::Name=\"XG Voice\"]/PC[attribute::PC=\"" 
			     + strPCno + "\"]/Bank");
			NodeList nlSelectedPCNodes = (NodeList)xpe1.evaluate(dc, XPathConstants.NODESET);
 			//各ノードの属性(attr)を取得
 			NamedNodeMap nnmAttribs;
 			Node nd;
			String voiceName;
			int LSBvalue = 0;
 			for(int i=0; i< nlSelectedPCNodes.getLength(); i++){
 				nd = nlSelectedPCNodes.item(i);
 				if(nd.getNodeName() == "Bank") {
 					nnmAttribs = nd.getAttributes();
 					LSBvalue = Integer.parseInt(nnmAttribs.getNamedItem("LSB").getTextContent().trim());
					voiceName = nnmAttribs.getNamedItem("Name").getTextContent().trim();
 					m.put(String.valueOf(LSBvalue), voiceName);
 				}
 			}
			
		} catch (XPathExpressionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		return m;
	}
	public LinkedHashMap<String, String> getXGDrumNames(int PCno){
		String strPCno = "  1";
		LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();

		try {
			xpe1 = xp.compile("/ModuleData/DrumSetList/Map[attribute::Name=\"XG Drum\"]/PC[attribute::PC=\"" 
			     + strPCno + "\"]/Bank/Tone");
			NodeList nlSelectedPCNodes = (NodeList)xpe1.evaluate(dc, XPathConstants.NODESET);
 			//各ノードの属性(attr)を取得
 			NamedNodeMap nnmAttribs;
 			Node nd;
			String drumName;
			int drumKey = 0;
 			for(int i=0; i< nlSelectedPCNodes.getLength(); i++){
 				nd = nlSelectedPCNodes.item(i);
 				if(nd.getNodeName() == "Tone") {
 					nnmAttribs = nd.getAttributes();
 					drumKey = Integer.parseInt(nnmAttribs.getNamedItem("Key").getTextContent().trim());
					drumName = nnmAttribs.getNamedItem("Name").getTextContent().trim();
 					m.put(String.valueOf(drumKey), drumName);
 					System.out.println("drumset:" + drumKey + " " + drumName);
 				}
 			}
			
		} catch (XPathExpressionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		return m;
	}
	public int getPrevUseVoiceNo(int voiceNo){
		while(mapVoiceList.get(String.valueOf(--voiceNo)).isMarked || voiceNo==1){
//			voiceNo--;
		}
		return voiceNo;
	}
	public int getNextUseVoiceNo(int voiceNo){
		while(mapVoiceList.get(String.valueOf(++voiceNo)).isMarked || voiceNo==128){
//			voiceNo++;
		}
		return voiceNo;
	}
	
	private void parseXML(){
		try {
			//xmlファイルを読み込み、DOMツリーを構築
			factory = DocumentBuilderFactory.newInstance();
			builder=factory.newDocumentBuilder();
			input = new URL("file:./resource/_mu50_utf8.xml").openStream();
			dc = builder.parse(input);

			//XPathを構築し、検索準備
			xpf = XPathFactory.newInstance();
			xp = xpf.newXPath();
			xpe1 = xp.compile("/ModuleData/InstrumentList/Map[attribute::Name=\"XG Voice\"]/node()");

			//XPathから(XG128音色および同レベルの)ノードを取得
 			NodeList nlPCNodes = (NodeList)xpe1.evaluate(dc, XPathConstants.NODESET);

 			//各ノードの属性(attr)を取得
 			NamedNodeMap nnmAttribs;
 			Node nd;
			String voiceName;
			String use;
			VoiceInfo vinfo = null;
			int groupNo = 0;
			int PCvalue;
			NodeList nlXGVoices;

			for(int i=0; i<nlPCNodes.getLength(); i++){
 				nd = nlPCNodes.item(i);
// 				System.out.println(nd.getNodeName() + " " + nd.getNodeType());
 				
 				//コメントノードからグループ名を取得
 				if (nd.getNodeType() == Node.COMMENT_NODE ){
// 					System.out.println(nd.getNodeValue());
 					GroupNames[groupNo] = nd.getNodeValue();
 					groupNo++;
 				}
 				//PCノードから128音色のパラメータを取得
 				if (nd.getNodeName() == "PC") {
 					nnmAttribs = nd.getAttributes();
 					
 					use = nnmAttribs.getNamedItem("Use").getTextContent().trim();
					PCvalue = Integer.parseInt(nnmAttribs.getNamedItem("PC").getTextContent().trim());
					voiceName = nnmAttribs.getNamedItem("Name").getTextContent().trim();
 					vinfo = new VoiceInfo(voiceName, use);
 					mapVoiceList.put(String.valueOf(PCvalue),vinfo);
//	 				System.out.println("PC# Name LSB:" + PCvalue + " " + voiceName + " " + LSBvalue);

	 				//BankノードからXG音色のパラメータを取得
	 				nlXGVoices = nd.getChildNodes();
	 				int LSBcount = 0;

	 				for(int j=0; j<nlXGVoices.getLength(); j++){
	 					nd = nlXGVoices.item(j);
	 					if (nd.getNodeName() == "Bank"){
		 					nnmAttribs = nd.getAttributes();
		 					
							voiceName = nnmAttribs.getNamedItem("Name").getTextContent().trim();
//			 				System.out.println("grp PC# Name LSB:" + (groupNo-1) + GroupNames[groupNo-1] + " " + PCvalue + " " + voiceName + " " + LSBvalue);
			 				XGVoiceInfo[groupNo-1][i%8][j] = voiceName;
			 				
			 				LSBcount++;
	 					}
	 				}
	 				System.out.println("bank lsb : " + PCvalue + " " + LSBcount);
 				}
 			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
