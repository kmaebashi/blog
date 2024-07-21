package com.kmaebashi.blog.service;

import com.kmaebashi.blog.common.ApiStatus;
import com.kmaebashi.blog.controller.data.CommentData;
import com.kmaebashi.blog.controller.data.PostCommentStatus;
import com.kmaebashi.blog.dbaccess.CommentDbAccess;
import com.kmaebashi.blog.dbaccess.ProfileDbAccess;
import com.kmaebashi.blog.dto.ProfileDto;
import com.kmaebashi.jsonparser.ClassMapper;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.InvokerOption;
import com.kmaebashi.nctfw.JsonResult;
import com.kmaebashi.nctfw.ServiceInvoker;

import java.time.LocalDateTime;

public class CommentService {
    public static JsonResult postComment(ServiceInvoker invoker, String posterUserId, String blogId, CommentData comment) {
        return invoker.invoke((context) -> {
            if (posterUserId != null) {
                ProfileDto profileDto = ProfileDbAccess.getProfileByUserId(context.getDbAccessInvoker(), posterUserId);
                if (profileDto == null
                    || !profileDto.nickname.equals(comment.poster)) {
                    throw new BadRequestException("投稿者のニックネームとログインユーザのニックネームが一致しません。"
                            + " 投稿者.." + comment.poster + ", ログインユーザ.." + profileDto.nickname);
                }
            }
            CommentDbAccess.insertComment(context.getDbAccessInvoker(),
                                          comment.blogPostId, posterUserId, comment.poster, comment.message);
            PostCommentStatus status = new PostCommentStatus(ApiStatus.SUCCESS, "投稿成功");
            String json = ClassMapper.toJson(status);

            return new JsonResult(json);
        }, InvokerOption.TRANSACTIONAL);
    }

}
