package com.wln.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class FaceUtil {
	
	
	/**
	 * 根据传入图片进行人脸检测
	 * @param file 传入的人脸照片
	 * @return 返回人脸照片的facetoken,如果为空说明图片不符合要求
	 * @throws Exception
	 */
	public static String detect(File file) throws Exception {
		byte[] buff = HTTPUtil.getBytesFromFile(file);
		String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
		HashMap<String, byte[]> byteMap = new HashMap<>();
		byteMap.put("image_file", buff);
		byte[] bacd = HTTPUtil.post(url, HTTPUtil.map, byteMap);
		String str = new String(bacd);
		if(str.indexOf("error_message")!=-1) {
			return null;
		}
		JSONObject obj = JSONObject.parseObject(str);
		int faceNum=obj.getIntValue("face_num");
		if(faceNum==1) {
			//获取facetoken
			JSONObject face=(JSONObject) ((JSONArray)obj.get("faces")).get(0);
			String faceToken=face.getString("face_token");
			return faceToken;
		}
		return null;
	}
	
	/**
	 * 查询指定的照片是否在人脸集合faceset中存在
	 * @param faceToken
	 * @return
	 * @throws Exception
	 */
	public static boolean search(String faceToken) throws Exception {
		String url = "https://api-cn.faceplusplus.com/facepp/v3/search";
		HTTPUtil.map.put("face_token", faceToken);
		byte[] bacd = HTTPUtil.post(url, HTTPUtil.map, null);
		String str = new String(bacd);
		if(str.indexOf("error_message")==-1) {//请求没有错误
			JSONObject json = JSONObject.parseObject(str);
			JSONObject thresholds=(JSONObject) json.get("thresholds");
			Double le5=thresholds.getDouble("1e-5");
			JSONArray results=(JSONArray) json.get("results");
			if(results!=null && results.size()>=1) {
				Double confidence=((JSONObject)results.get(0)).getDouble("confidence");
				if(confidence>le5) {
					return true;
				}
			}
		}
		return false;
	}	

	/**
	 * 添加人脸到faceset中
	 * @param face_tokens 要添加的人脸
	 * @return 
	 * @throws Exception
	 */
	public static boolean addFace(String face_tokens) throws Exception {
		if(!getDetail()) {//先获取人脸集合，没有集合就创建一个
			System.out.println("没有获取到指定人脸集合");
			boolean res=createFaceSet();
			if(!res) {
				System.out.println("创建人脸集合出问题了!");
				return false;
			}
			System.out.println("创建人脸集合成功！");
		}
		String url = "https://api-cn.faceplusplus.com/facepp/v3/faceset/addface";
		HTTPUtil.map.put("face_tokens", face_tokens);
		byte[] bacd = HTTPUtil.post(url, HTTPUtil.map, null);
		String str = new String(bacd);
		if(str.indexOf("error_message")!=-1) {
			return false;
		}
		return true;
	}	
	
	
	/**
	 * 创建一个人脸的集合 FaceSet，用于存储人脸标识 face_token。
	 * @return
	 * @throws Exception
	 */
	private static boolean createFaceSet() throws Exception {
		String url = "https://api-cn.faceplusplus.com/facepp/v3/faceset/create";
		byte[] bacd = HTTPUtil.post(url, HTTPUtil.map, null);
		String str = new String(bacd);
		System.out.println(str);
		if(str.indexOf("error_message")!=-1) {
			return false;
		}
		System.out.println("创建人脸集合："+str);
		return true;
	}
	
	/**
	 * 获取一个faceset
	 * @return
	 * @throws Exception
	 */
	private static boolean getDetail() throws Exception {
		String url = "https://api-cn.faceplusplus.com/facepp/v3/faceset/getdetail";
		byte[] bacd = HTTPUtil.post(url, HTTPUtil.map, null);
		String str = new String(bacd);
		if(str.indexOf("error_message")!=-1) {
			return false;
		}
		return true;
	}

}
