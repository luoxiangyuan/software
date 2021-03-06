package com.software.soft.controller;

import com.software.common.entity.PageResult;
import com.software.common.entity.Result;
import com.software.common.entity.StatusCode;
import com.software.common.util.FastDFSUtil;
import com.software.soft.pojo.Soft;
import com.software.soft.pojo.UserSoftDownload;
import com.software.soft.service.SoftService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/soft")
@Api(tags = "软件相关接口")
@CrossOrigin(allowCredentials="true",maxAge = 3600)
public class SoftController {

    @Autowired
    private SoftService softService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @RequestMapping(value = "/download",method = RequestMethod.GET)
    @ApiOperation(value = "软件下载")
    public void download(@RequestParam(value = "name") String name,@RequestParam(value = "url") String url) throws Exception {
        //group1-M00/00/00/wKhihF4hZwmAJoXHAAAXA0yMGTo637.png

        //下载软件需要登录 user权限
        Claims claims = (Claims) request.getAttribute("user_claims");
        if(claims == null){
           throw new Exception("请登录");
        }

        try {

            Soft soft = new Soft();
            soft.setName(name);
            soft.setUrl(url);
            softService.download(soft);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // return new Result(true,"下载成功",StatusCode.OK);

    }



    @RequestMapping(value = "/search/{page}/{size}",method = RequestMethod.POST)
    @ApiOperation( value = "查询软件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10")
    }
    )
    public Result search(@PathVariable int page,@PathVariable int size,@RequestBody Soft soft){

         Page pages = softService.search(page, size,soft);

         return new Result(true,"执行成功", StatusCode.OK,new PageResult<>(pages.getTotalElements(),pages.getContent()));
    }

    @RequestMapping(value = "/{classifyId}/{page}/{size}",method = RequestMethod.GET)
    @ApiOperation( value = "根据类别查询软件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "classifyId", value = "类别id"),
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10")
    }
    )
    public Result findByClassifyId(@PathVariable String classifyId,@PathVariable int page,@PathVariable int size){

        Page pages = softService.findByClassifyId(classifyId,page,size);
        return new Result(true,"执行成功", StatusCode.OK,new PageResult<>(pages.getTotalElements(),pages.getContent()));
    }

    @RequestMapping(value = "/hotList/{page}/{size}",method = RequestMethod.GET)
    @ApiOperation( value = "热门(推荐)软件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10")
    }
    )
    public Result findHotList(@PathVariable int page ,@PathVariable int size){
        Page pages = softService.findHotList(page,size);
        return new Result(true,"执行成功", StatusCode.OK,new PageResult<>(pages.getTotalElements(),pages.getContent()));
    }

    @RequestMapping(value = "/newList/{page}/{size}",method = RequestMethod.GET)
    @ApiOperation( value = "最新软件列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10")
    }
    )
    public Result findNewList(@PathVariable int page ,@PathVariable int size){
        Page pages = softService.findNewList(page,size);
        return new Result(true,"执行成功", StatusCode.OK,new PageResult<>(pages.getTotalElements(),pages.getContent()));
    }


    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "通过id查询软件详情")
    @ApiImplicitParam(name = "id",value = "软件id",required = true)
    public Result findById(@PathVariable String id){
        Soft soft = softService.findById(id);
        return new Result(true,"执行成功",StatusCode.OK,soft);
    }

    @RequestMapping(value = "/thumb/{id}",method = RequestMethod.PUT)
    @ApiOperation(value = "点赞软件，软件的点赞数加一")
    @ApiImplicitParam(name = "id",value = "软件id",required = true)
    public Result thumb(@PathVariable String id){
        softService.thumb(id);
        return new Result(true,"点赞成功",StatusCode.OK);
    }

    @RequestMapping(value = "/updateDownload/{id}",method = RequestMethod.PUT)
    @ApiOperation(value = "软件的下载数加一")
    @ApiImplicitParam(name = "id",value = "软件id",required = true)
    public Result updateDownload(@PathVariable String id){
        softService.updateDownload(id);
        return new Result(true,"操作成功",StatusCode.OK);
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ApiOperation(value = "添加软件")
    public Result add(@RequestBody Soft soft){
            softService.add(soft);

            return new Result(true,"添加成功",StatusCode.OK);
    }
    @RequestMapping(value = "/findNoTutorial",method = RequestMethod.GET)
    @ApiOperation(value = "查询没有教程的软件")
    public Result findNoTutorial(){
        List<Soft> softList = softService.findNoTutorial();
        return new Result(true,"查询成功",StatusCode.OK,softList);
    }

    //上传图片
    @RequestMapping(value = "/uploadPic",method = RequestMethod.POST, headers="content-type=multipart/form-data")
    public Result uploadPic(@RequestParam(value = "file") MultipartFile file){
        try {
            //上传图片
            //获取图片字节数组
            byte [] picFile = file.getBytes();
            //文件扩展名
            String picExtName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            //文件链接
            String picUrl = fastDFSUtil.uploadPic(picFile, picExtName);

            Map map = new HashMap();
            map.put("name",file.getOriginalFilename());
            map.put("url",picUrl);

            return new Result(true,"上传成功",StatusCode.OK,map);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false,"上传失败",StatusCode.ERROR);
        }

    }



    //上传软件
    @RequestMapping(value = "/uploadFile",method = RequestMethod.POST, headers="content-type=multipart/form-data")
    public Result uploadFile(@RequestParam(value = "file") MultipartFile file){
        try {
            byte [] uploadFile = file.getBytes();
            //文件扩展名
            String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            //文件链接
            String url = fastDFSUtil.upload(uploadFile, extName);

            Map map = new HashMap();
            map.put("name",file.getOriginalFilename());
            map.put("url",url);

            return new Result(true,"上传成功",StatusCode.OK,map);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false,"上传失败",StatusCode.ERROR);
        }

    }

    @RequestMapping(value = "/delete/{id}",method = RequestMethod.POST)
    @ApiOperation(value = "根据id删除软件")
    public Result deleteById(@PathVariable String id){
        softService.deleteById(id);
        return new Result(true,"删除成功",StatusCode.OK);
    }

    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    @ApiOperation(value = "更新软件")
    public Result update(@RequestBody Soft soft){
        softService.update(soft);
        return new Result(true,"更新成功",StatusCode.OK);
    }

    //查询用户点赞的软件列表
    @RequestMapping(value = "/userThumb/{userId}",method = RequestMethod.GET)
    @ApiOperation(value = "查询用户点赞的软件列表")
    public Result userThumb(@PathVariable String userId){
        List<Soft> softList = softService.userThumb(userId);
        return new Result(true,"查询成功",StatusCode.OK,softList);
    }

    //查询用户下载过的软件列表
    @RequestMapping(value = "/userDownload/{userId}",method = RequestMethod.GET)
    @ApiOperation(value = "查询用户下载的软件列表")
    public Result userDownload(@PathVariable String userId){
        List<UserSoftDownload> userSoftDownloads = softService.userDownload(userId);
        return new Result(true,"查询成功",StatusCode.OK,userSoftDownloads);
    }

    //修改是否有教程字段
    @RequestMapping(value = "/updateTutorial/{tutorial}/{id}",method = RequestMethod.PUT)
    @ApiOperation(value = "查询用户下载的软件列表")
    public void updateTutorial(@PathVariable int tutorial,@PathVariable String id){
        softService.updateTutorial(tutorial,id);
    }

    //评分软件
    @RequestMapping(value = "/doRate",method = RequestMethod.POST)
    @ApiOperation(value = "评分软件")
    public Result doRate(@RequestBody UserSoftDownload userSoftDownload){
        softService.doRate(userSoftDownload);
        return new Result(true,"评分成功",StatusCode.OK);
    }


    //软件列表  根据类别分类
    @RequestMapping(value = "/classifySoft",method = RequestMethod.GET)
    @ApiOperation(value = "根据类别 分类软件列表显示")
    public Result classifySofts(){
        return new Result(true,"查询成功",StatusCode.OK,softService.classifySofts());
    }

    /*@RequestMapping(value = "/add",method = RequestMethod.POST, headers="content-type=multipart/form-data")
    @ApiOperation(value = "添加软件")
    public Result add(@RequestParam(value = "file") MultipartFile file,
                      @RequestParam(value = "pic") MultipartFile pic,
                      @RequestParam(value = "classifyId") String classifyId,
                      @RequestParam(value = "introduction") String introduction,
                      @RequestParam(value = "name") String name){
        try {


            //忽略元素为空判断
            Soft soft = new Soft();
            soft.setClassifyId(classifyId);
            soft.setIntroduction(introduction);
            soft.setName(name);

            //上传软件
            //获取文件字节数组
            byte [] uploadFile = file.getBytes();
            //文件扩展名
            String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            //文件链接
            String url = fastDFSUtil.upload(uploadFile, extName);
            soft.setUrl(url);

            //上传图片
            //获取图片字节数组
            byte [] picFile = pic.getBytes();
            //文件扩展名
            String picExtName = pic.getOriginalFilename().substring(pic.getOriginalFilename().lastIndexOf(".") + 1);
            //文件链接
            String picUrl = fastDFSUtil.uploadPic(picFile, picExtName);
            soft.setPic(picUrl);

            //上传软件到fastdfs
            softService.add(soft);
            return new Result(true,"添加成功",StatusCode.OK);
        } catch (IOException e) {

            e.printStackTrace();
            return new Result(false,"文件上传失败",StatusCode.ERROR);
        }

    }*/

}
