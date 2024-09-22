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

  const sectionBoxElemList = document.getElementById("section-container")
                                     .getElementsByClassName("section-box");
  for (let i = 0; i < sectionBoxElemList.length; i++) {
    refreshSectionAttr(sectionBoxElemList[i], i + 1);
  }
});

function refreshSectionAttr(secElem, section) {
  secElem.id = "section-box" + section;
  secElem.dataset.section = section;

  const deleteSectionButtonElem = secElem.getElementsByClassName("section-delete-button")[0]
  deleteSectionButtonElem.dataset.section = section;
  deleteSectionButtonElem.onclick = deleteSection;

  const photoElemList = secElem.getElementsByClassName("one-photo");
  for (let i = 0; i < photoElemList.length; i++) {
    photoElemList[i]

    const deleteButtonElem = photoElemList[i].getElementsByClassName("photo-delete-button")[0];
    deleteButtonElem.dataset.section = section;
    deleteButtonElem.dataset.photoIndex = i;
    deleteButtonElem.onclick = deletePhoto;

    const upButtonElem = photoElemList[i].getElementsByClassName("photo-up-button")[0];
    if (i === 0) {
      upButtonElem.remove();
    } else {
      upButtonElem.dataset.section = section;
      upButtonElem.dataset.photoIndex = i;
      upButtonElem.onclick = upPhoto;
    }

    const downButtonElem = photoElemList[i].getElementsByClassName("photo-down-button")[0];
    if (i === photoElemList.length - 1) {
      downButtonElem.remove();
    } else {
      downButtonElem.dataset.section = section;
      downButtonElem.dataset.photoIndex = i;
      downButtonElem.onclick = downPhoto;
    }
  }
}

// photosInThisPageはHTML側にサーバで展開される。
// photosInThisPageの構造
// {
//   "section1": [
//     {"id": <画像ID>, "caption": "キャプション"},
//     {"id": <画像ID>, "caption": "キャプション"},
//     {"id": <画像ID>, "caption": "キャプション"}
//   ],
//   ...
// }

function imageFileInputOnChange(event) {
  const files = event.target.files;
  if (files.length == 0) {
    return;
  }
  const section = parseInt(event.target.dataset.section);
  const url = "./api/postimages";
  const formData = new FormData();
  formData.append("section", section);
  for (let i = 0; i < files.length; i++) {
    formData.append("file" + i, files[i]);
  }
  const uploadingDialog = document.getElementById("now-uploading-dialog");
  uploadingDialog.showModal();
  console.log("showModal()");
  let req = new Request(url, {
      body: formData,
      method: "POST",
      mode: "no-cors"
    });
    fetch(req)
      .then((response) => {
        console.log("fetch pass1");
        return response.json()
      })
      .then((result) => {
        console.log("fetch pass2");
        addPhotos(section, result);
        console.log("fetch pass3");
        refreshSectionPhotos(section);
        console.log("fetch pass4");
        const sectionDiv = document.getElementById("section-box" + section);
        refreshSectionAttr(sectionDiv, section);
        uploadingDialog.close();
        console.log("close()");
      })
      .catch((e) => {
        console.warn("画像のアップロードでエラー" + e.message);
      });
  event.target.value = "";
}

function addPhotos(section, newPhotoArray) {
  let sectionPhotos;

  if (("section" + section) in photosInThisPage) {
    sectionPhotos = photosInThisPage["section" + section];
  } else {
    sectionPhotos = [];
  }
  photosInThisPage["section" + section] = sectionPhotos.concat(newPhotoArray);
}

function refreshSectionPhotos(section) {
  console.log("refreshSectionPhotos pass1");
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
  console.log("refreshSectionPhotos pass3");
  const sectionPhotos = photosInThisPage["section" + section];
  const onePhotoTemplateElem = document.getElementById("hidden-section-box")
                                       .getElementsByClassName("one-photo")[0];
  console.log("refreshSectionPhotos pass4");

  for (let i = 0; i < sectionPhotos.length; i++) {
    const newOnePhotoElem = onePhotoTemplateElem.cloneNode(true);
    const imgElem = newOnePhotoElem.getElementsByClassName("photo")[0];
    imgElem.setAttribute("src", "./api/getimageadmin/" + sectionPhotos[i].id);

    const captionElem = newOnePhotoElem.getElementsByClassName("photo-caption")[0];
    captionElem.setAttribute("id", "photo-caption-" + sectionPhotos[i].id);
    captionElem.value = sectionPhotos[i].caption;

    photoDiv.appendChild(newOnePhotoElem);
  }
  console.log("refreshSectionPhotos pass5");
}

function deletePhoto(event) {
  const section = parseInt(event.currentTarget.dataset.section);
  const photoIndex = parseInt(event.currentTarget.dataset.photoIndex);

  saveCaptions(section);
  photosInThisPage["section" + section].splice(photoIndex, 1);
  refreshSectionPhotos(section);
  refreshSectionAttr(document.getElementById("section-box" + section), section);
}

function upPhoto(event) {
  const section = parseInt(event.currentTarget.dataset.section);
  const photoIndex = parseInt(event.currentTarget.dataset.photoIndex);

  saveCaptions(section);
  const temp = photosInThisPage["section" + section][photoIndex - 1];
  photosInThisPage["section" + section][photoIndex - 1]
                = photosInThisPage["section" + section][photoIndex];
  photosInThisPage["section" + section][photoIndex] = temp;

  refreshSectionPhotos(section);
  refreshSectionAttr(document.getElementById("section-box" + section), section);
}

function downPhoto(event) {
  const section = parseInt(event.currentTarget.dataset.section);
  const photoIndex = parseInt(event.currentTarget.dataset.photoIndex);

  saveCaptions(section);
  const temp = photosInThisPage["section" + section][photoIndex + 1];
  photosInThisPage["section" + section][photoIndex + 1]
                = photosInThisPage["section" + section][photoIndex];
  photosInThisPage["section" + section][photoIndex] = temp;

  refreshSectionPhotos(section);
  refreshSectionAttr(document.getElementById("section-box" + section), section);
}

function saveCaptions(section) {
  const photoArray = photosInThisPage["section" + section];
  const sectionElem = document.getElementById("section-box" + section);
  const captionList = sectionElem.getElementsByClassName("photo-caption");
  if (photoArray.length != captionList.length) {
    alert("画像の配列が内部的に不整合を起こしています。");
  }
  for (let i = 0; i < photoArray.length; i++) {
    photoArray[i].caption = captionList[i].value;
  }
}

function addSection() {
  const sectionContainer = document.getElementById("section-container");
  const existingSections = sectionContainer.getElementsByClassName("section-box");
  const sectionNumber = existingSections.length + 1;

  const cloneSectionDiv = document.getElementById("hidden-section-box").cloneNode(true);
  cloneSectionDiv.setAttribute("id", "section-box" + sectionNumber);
  cloneSectionDiv.removeAttribute("style");
  cloneSectionDiv.getElementsByClassName("one-photo")[0].remove();
  const sectionTitle = cloneSectionDiv.getElementsByClassName("section-title")[0];
  sectionTitle.innerText = "セクション" + sectionNumber;
  const fileInputButton = cloneSectionDiv.getElementsByClassName("image-file-input")[0];
  fileInputButton.setAttribute("data-section", sectionNumber);
  fileInputButton.onchange = imageFileInputOnChange;

  sectionContainer.appendChild(cloneSectionDiv);
}

function deleteSection(event) {
  const section = parseInt(event.currentTarget.dataset.section);

  const beforeSectionCount = Object.keys(photosInThisPage).length;
  if (beforeSectionCount === 1) {
    alert("ひとつしかないセクションは削除できません。");
    return;
  }
  if (!confirm("セクションを削除します。よろしいですか?")) {
    return;
  }
  const targetElem = document.getElementById("section-box" + section);
  targetElem.remove();
  delete photosInThisPage["section" + section];
  for (let i = section + 1; i <= beforeSectionCount; i++) {
    saveCaptions(i);
    photosInThisPage["section" + (i - 1)] = photosInThisPage["section" + i];
    delete photosInThisPage["section" + i];
    const sectionBoxElem = document.getElementById("section-box" + i);
    sectionBoxElem.id = "section-box" + (i - 1);
    sectionBoxElem.dataset.section = (i - 1);
  }

  for (let i = 1; i <= beforeSectionCount - 1; i++) {
    const sectionBoxElem = document.getElementById("section-box" + i);
    refreshSectionPhotos(i);
    refreshSectionAttr(sectionBoxElem, i);
  }
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
