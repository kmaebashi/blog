"use strict";

document.addEventListener("DOMContentLoaded", function(event) {
  const sectionContainer = document.getElementById("section-container");
  const fileInputButtons = sectionContainer.getElementsByClassName("image-file-input");

  for (let i = 0; i < fileInputButtons.length; i++) {
    fileInputButtons[i].onchange = imageFileInputOnChange;
  }

  const addSectionButton = document.getElementById("add-section-button");
  addSectionButton.onclick = addSection;

  const saveButton = document.getElementById("save-button");
  saveButton.onclick = save;

  const publishButton = document.getElementById("publish-button");
  publishButton.onclick = publish;
});

// photosInThisPageはHTML側にサーバで展開される。
// photosInThisPageの構造
// {
//   "section1": [
//     {"id": <画像ID>},
//     {"id": <画像ID>},
//     {"id": <画像ID>}
//   ],
//   ...
// }

function imageFileInputOnChange(event) {
  console.log("imageFileInputOnChange pass1");
  const files = event.target.files;
  if (files.length == 0) {
    return;
  }
  console.log("imageFileInputOnChange pass2 files.length.." + files.length);
  const section = event.target.dataset.section;
  const url = "./api/postimages";
  const formData = new FormData();
  formData.append("section", section);
  for (let i = 0; i < files.length; i++) {
    formData.append("file" + i, files[i]);
  }
  let req = new Request(url, {
      body: formData,
      method: "POST",
      mode: "no-cors"
    });
    fetch(req)
      .then((response) => {
        console.log("imageFileInputOnChange pass3");
        return response.json()
      })
      .then((result) => {
        console.log("imageFileInputOnChange pass4");
        addPhotos(section, result);
        refreshSectionPhotos(section);
      })
      .catch((e) => {
        console.warn
      });
  event.target.value = "";
}

function addPhotos(section, newPhotoArray) {
  let sectionPhotos;

  console.log("addPhotos pass1 len.." + newPhotoArray.length);
  if (("section" + section) in photosInThisPage) {
    console.log("addPhotos pass2-1");
    sectionPhotos = photosInThisPage["section" + section];
  } else {
    console.log("addPhotos pass2-2");
    sectionPhotos = [];
  }
  console.log("addPhotos pass3");
  photosInThisPage["section" + section] = sectionPhotos.concat(newPhotoArray);
}

function refreshSectionPhotos(section) {
  console.log("refreshSectionPhotos pass1 section.." + section);
  if (!("section" + section) in photosInThisPage) {
    console.log("ないはずはない");
    return;
  }
  console.log("refreshSectionPhotos pass2");
  const sectionDiv = document.getElementById("section-box" + section);
  const photoDiv = sectionDiv.getElementsByClassName("photo-area")[0];
  while (photoDiv.firstChild) {
    photoDiv.removeChild(photoDiv.firstChild);
  }
  const sectionPhotos = photosInThisPage["section" + section];

  console.log("refreshSectionPhotos pass3 len.." + sectionPhotos.length);
  for (let i = 0; i < sectionPhotos.length; i++) {
    console.log("refreshSectionPhotos pass4 i.." + i);
    const imageElem = document.createElement("img");
    imageElem.setAttribute("src", "./api/getimageadmin/" + sectionPhotos[i].id);
    const pElem = document.createElement("p");
    pElem.appendChild(imageElem);
    const captionElem = document.createElement("textarea");
    captionElem.setAttribute("id", "photo-caption-" + sectionPhotos[i].id);
    captionElem.setAttribute("class", "photo-caption");
    const onePhotoDiv = document.createElement("div");
    onePhotoDiv.setAttribute("class", "one-photo");
    onePhotoDiv.appendChild(pElem);
    onePhotoDiv.appendChild(captionElem);
    photoDiv.appendChild(onePhotoDiv);
  }
}

function addSection() {
  const sectionContainer = document.getElementById("section-container");
  const existingSections = sectionContainer.getElementsByClassName("section-box");
  const sectionNumber = existingSections.length + 1;

  const cloneSectionDiv = document.getElementById("hidden-section-box").cloneNode(true);
  cloneSectionDiv.setAttribute("id", "section-box" + sectionNumber);
  cloneSectionDiv.removeAttribute("style");
  const sectionTitle = cloneSectionDiv.getElementsByClassName("section-title")[0];
  sectionTitle.innerText = "セクション" + sectionNumber;
  const fileInputButton = cloneSectionDiv.getElementsByClassName("image-file-input")[0];
  fileInputButton.setAttribute("data-section", sectionNumber);
  fileInputButton.onchange = imageFileInputOnChange;

  sectionContainer.appendChild(cloneSectionDiv);
}

function save() {
  postArticle(false);
}

function publish() {
  postArticle(true);
}

function postArticle(publishFlag) {
  const post = {};

  const params = new URLSearchParams(document.location.search);

  post.blogPostId = parseInt(params.get("postid"));
  post.title = document.getElementById("blog-post-title").value;
  post.publishFlag = publishFlag;

  const sectionContainer = document.getElementById("section-container");
  const sectionList = sectionContainer.getElementsByClassName("section-box");
  post.sectionArray = [];

  for (let secIdx = 0; secIdx < sectionList.length; secIdx++) {
    const section = {};
    section["id"] = "section" + (secIdx + 1);
    section["body"] = sectionList[secIdx].getElementsByClassName("section-text")[0].value;
    section["photos"] = [];
    if (("section" + (secIdx + 1)) in photosInThisPage) {
      const photos = photosInThisPage["section" + (secIdx + 1)];
      for (let photoIdx = 0; photoIdx < photos.length; photoIdx++) {
        const photoObj = {};
        photoObj.id = photos[photoIdx].id;
        photoObj.caption = document.getElementById("photo-caption-" + photos[photoIdx].id).value;
        section.photos.push(photoObj);
      }
    }
    post.sectionArray.push(section);
  }
  const metaElem = document.querySelector('meta[name="csrf_token"]');
  let csrfToken = "";
  if (metaElem !== null) {
    csrfToken = metaElem.content;
  }
  console.log("csrfToken.." + csrfToken);
  fetch("./api/postarticle", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Csrf-Token": csrfToken
        },
        body: JSON.stringify(post)
    })
    .then(response => {
      return response.text()
    })
    .then(ret => {
      console.log(ret);
      const retObj = JSON.parse(ret);
      location.href = "./admin?postid=" + retObj.blogPostId;
    });
}
