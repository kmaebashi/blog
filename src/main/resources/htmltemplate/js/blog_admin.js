"use strict";

document.addEventListener("DOMContentLoaded", function(event) {
  const sectionContainer = document.getElementById("section-container");
  const fileInputButtons = sectionContainer.getElementsByClassName("image-file-input");

  for (let i = 0; i < fileInputButtons.length; i++) {
    fileInputButtons[i].onchange = imageFileInputOnChange;
  }

  const addSectionButton = document.getElementById("add-section-button");
  addSectionButton.onclick = addSection;
});

const photos = {
};

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
}

function addPhotos(section, newPhotoArray) {
  let sectionPhotos;

  console.log("addPhotos pass1 len.." + newPhotoArray.length);
  if (("section" + section) in photos) {
    console.log("addPhotos pass2-1");
    sectionPhotos = photos["section" + section];
  } else {
    console.log("addPhotos pass2-2");
    sectionPhotos = [];
  }
  console.log("addPhotos pass3");
  photos["section" + section] = sectionPhotos.concat(newPhotoArray);
}

function refreshSectionPhotos(section) {
  console.log("refreshSectionPhotos pass1 section.." + section);
  if (!("section" + section) in photos) {
    return;
  }
  console.log("refreshSectionPhotos pass2");
  const sectionDiv = document.getElementById("section-box" + section);
  const photoDiv = sectionDiv.getElementsByClassName("photo-area")[0];
  while (photoDiv.firstChild) {
    photoDiv.removeChild(photoDiv.firstChild);
  }
  const sectionPhotos = photos["section" + section];

  console.log("refreshSectionPhotos pass3 len.." + sectionPhotos.length);
  for (let i = 0; i < sectionPhotos.length; i++) {
    console.log("refreshSectionPhotos pass4 i.." + i);
    const imageElem = document.createElement("img");
    imageElem.setAttribute("src", "./api/getimageadmin/" + sectionPhotos[i].id);
    const pElem = document.createElement("p");
    pElem.appendChild(imageElem);
    const captionElem = document.createElement("textarea");
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
