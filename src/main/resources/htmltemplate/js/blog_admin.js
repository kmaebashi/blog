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

// elemはclassがsection-boxのdiv要素
function setPhotoButtonHandler(elem) {
  const section = parseInt(elem.dataset.section);
  const photoIndex = parseInt(elem.dataset.photoIndex);

  const deleteButtonElem = elem.getElementsByClassName("photo_delete_button")[0];
  deleteButtonElem.dataset.section = section;
  deleteButtonElem.dataset.photoIndex = photoIndex;
  deleteButtonElem.onclick = deletePhoto;

  const upButtonElem = elem.getElementsByClassName("photo_up_button")[0];
  if (photoIndex === 0) {
    upButtonElem.remove();
  } else {
    upButtonElem.dataset.section = section;
    upButtonElem.dataset.photoIndex = photoIndex;
    upButtonElem.onclick = upPhoto;
  }

  const downButtonElem = elem.getElementsByClassName("photo_down_button")[0];
  if (photoIndex === photosInThisPage["section" + section].length - 1) {
    downButtonElem.remove();
  } else {
    downButtonElem.dataset.section = section;
    downButtonElem.dataset.photoIndex = photoIndex;
    downButtonElem.onclick = downPhoto;
  }
}

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
  const onePhotoTemplateElem = document.getElementById("hidden-section-box")
                                       .getElementsByClassName("one-photo")[0];

  console.log("refreshSectionPhotos pass3 len.." + sectionPhotos.length);
  for (let i = 0; i < sectionPhotos.length; i++) {
    console.log("refreshSectionPhotos pass4 i.." + i);
    const newOnePhotoElem = onePhotoTemplateElem.cloneNode(true);
    const imgElem = newOnePhotoElem.getElementsByClassName("photo")[0];
    imgElem.setAttribute("src", "./api/getimageadmin/" + sectionPhotos[i].id);

    const captionElem = newOnePhotoElem.getElementsByClassName("photo-caption")[0];
    captionElem.setAttribute("id", "photo-caption-" + sectionPhotos[i].id);
    captionElem.value = sectionPhotos[i].caption;

    photoDiv.appendChild(newOnePhotoElem);
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

function deletePhoto(event) {
  console.log(event);
  const section = parseInt(event.currentTarget.dataset.section);
  const photoIndex = parseInt(event.currentTarget.dataset.photoIndex);

  photosInThisPage["section" + section].splice(photoIndex, 1);
  refreshSectionPhotos(section);
  refreshSectionAttr(document.getElementById("section-box" + section), section);
}

function upPhoto(event) {
  console.log(event);
}

function downPhoto(event) {
  console.log(event);
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
