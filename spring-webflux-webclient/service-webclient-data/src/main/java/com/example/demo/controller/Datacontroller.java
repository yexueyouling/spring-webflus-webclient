package com.example.demo.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.sf.json.JSONObject;

@RestController
public class Datacontroller {
	
	static class Book {
        String name;
        String title;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

	@GetMapping(value = "/getString")
	public String getString() {
		System.out.println("data------>getString");
		String s = "123asd中文@#￥%……";
		return s;
	}

	@GetMapping(value = "/getStringValue")
	public String getStringValue(@RequestParam String username,@RequestParam String param) {
		System.out.println("data------>getStringValue" + username);
		String s = "接受后返回的" + username + "-" +  param ;
		return s;
	}

	@GetMapping(value = "/getPathVarible/{username}/{age}")
	public String getPathVarible(@PathVariable("username") String username, @PathVariable("age") String age) {
		System.out.println("data------>getPathVarible" + username);
		String s = "接受后返回的" + username + "-" + age;
		return s;
	}

	@GetMapping(value = "/getStringMap")
	public String getStringMap(@RequestParam Map<String, Object> paramMap) {
		System.out.println("data------>getStringValue" + paramMap);
		String s = "接受后返回的" + paramMap.get("name") +  "-" + paramMap.get("age");
		return s;
	}

	
	@PostMapping(value = "/postMap")
	public String postMap (@RequestBody Map<String, Object> paramMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("aa", paramMap.get("username"));
		resultMap.put("bb", paramMap.get("age"));
		JSONObject jsonobj = JSONObject.fromObject(resultMap);
		String json = jsonobj.toString();
		return json;
	}
	
	@PostMapping(value = "/postFlux")
	public List<Book> postFlux (@RequestBody Map<String, Object> paramMap){
		List<Book> resultList = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Book book = new Book();
			book.setName("简述" + i);
			book.setTitle("title" + i);
			resultList.add(book);
		}
		System.out.println(resultList);
		return resultList;
	}
	
	@PostMapping(value = "/postFluxMap")
	public List<Book> postFluxMap (){
		List<Book> resultList = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Book book = new Book();
			book.setName("简述" + i);
			book.setTitle("title" + i);
			resultList.add(book);
		}
		System.out.println(resultList);
		return resultList;
	}
	
	@PostMapping(value = "/postForm")
	public String postForm (@RequestParam Map<String, Object> paramMap){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("aa", paramMap.get("username"));
		resultMap.put("bb", paramMap.get("age"));
		JSONObject jsonobj = JSONObject.fromObject(resultMap);
		String json = jsonobj.toString();
		return json;
	}
	
	
	@PostMapping(value = "/postBeanJson")
	public String postMap (@RequestBody String json){
		System.out.println(json);
		return json;
				
		
	}
	
	 @PostMapping(value = "/upload")
	    public String upload(@RequestParam("file") MultipartFile file) {
	        try {
	            if (file.isEmpty()) {
	                return "文件为空";
	            }
	            // 获取文件名
	            String fileName = file.getOriginalFilename();
	            System.out.println("上传的文件名为：" + fileName);
	            // 获取文件的后缀名
	            String suffixName = fileName.substring(fileName.lastIndexOf("."));
	            System.out.println("文件的后缀名为：" + suffixName);
	            // 设置文件存储路径
	            String filePath = "E:/picture/";
	            String path = filePath + fileName;
	            File dest = new File(path);
	            // 检测是否存在目录
	            if (!dest.getParentFile().exists()) {
	                dest.getParentFile().mkdirs();// 新建文件夹
	            }
	            file.transferTo(dest);// 文件写入
	            return "上传成功";
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return "上传失败";
	    }
	
	 @DeleteMapping(value = "/delete")
	 public String delete(@RequestParam String username) {
		 return "接受并返回来的删除参数：" + username;
	 }
	
	 
	 @PutMapping(value = "/put")
	public String put (@RequestBody Map<String, Object> paramMap){
		 Map<String, Object> resultMap = new HashMap<String, Object>();
		 resultMap.put("aa", paramMap.get("username"));
		 resultMap.put("bb", paramMap.get("age"));
		 JSONObject jsonobj = JSONObject.fromObject(resultMap);
		 String json = jsonobj.toString();
		 return "put后返回的传入后变更的json串：" + json;
	}
	 
	 
    @GetMapping("/download")
    public void downloadFile(HttpServletRequest request,
                               HttpServletResponse response) throws UnsupportedEncodingException {

        // 获取指定目录下的第一个文件
        File scFileDir = new File("E://music_eg");
        File TrxFiles[] = scFileDir.listFiles();
        System.out.println(TrxFiles[0]);
        String fileName = TrxFiles[0].getName(); //下载的文件名

        // 如果文件名不为空，则进行下载
        if (fileName != null) {
            //设置文件路径
            String realPath = "E://music_eg/";
            File file = new File(realPath, fileName);

            // 如果文件名存在，则进行下载
            if (file.exists()) {

                // 配置文件下载
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

                // 实现文件下载
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    System.out.println("Download the song successfully!");
                }
                catch (Exception e) {
                    System.out.println("Download the song failed!");
                }
                finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
	
	
}
