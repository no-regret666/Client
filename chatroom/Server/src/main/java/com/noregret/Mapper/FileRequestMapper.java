package com.noregret.Mapper;

import com.noregret.Pojo.FileRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileRequestMapper {
    @Insert("insert into fileRequest(fileID,`from`,`to`,status,filename) values (#{fileID},#{from},#{to},#{status},#{filename})")
    void insert(int fileID, String from, String to, int status,String filename);

    @Select("select * from fileRequest where `to` = #{username} and status = 2")
    List<FileRequest> findByUsername(String username);

    @Delete("delete from fileRequest where fileID = #{fileID} and `to` = #{username}")
    void delete(int fileID, String username);

    @Delete("delete from fileRequest where `from` = #{username} or `to` = #{username}")
    void deleteUser(String username);
}
