"use strict";

document.addEventListener("DOMContentLoaded", function(event) {
  const postCommentButton = document.getElementById("post-comment");
  postCommentButton.onclick = postComment;
});

function postComment() {
  const postData = {};
  const paths = location.pathname.split("/");
  postData.blogId = paths[2];
  postData.blogPostId = parseInt(paths[4]);
  postData.poster = document.getElementById("comment-poster-input").value;
  postData.message = document.getElementById("comment-textarea").value;

  const metaElem = document.querySelector('meta[name="csrf_token"]');
  let csrfToken = "";
  if (metaElem !== null) {
    csrfToken = metaElem.content;
  }
  console.log("csrfToken.." + csrfToken);
  fetch("../api/postcomment", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Csrf-Token": csrfToken
        },
        body: JSON.stringify(postData)
    })
    .then(response => {
      return response.text()
    })
    .then(ret => {
      console.log(ret);
      const retObj = JSON.parse(ret);
      location.reload();
    });

}