document.addEventListener("DOMContentLoaded", function(event) {
  const sectionContainer = document.getElementById("section-container");
  const fileInputButtons = sectionContainer.getElementsByClassName("image-file-input");

  for (let i = 0; i < fileInputButtons.length; i++) {
    fileInputButtons[i].onchange = imageFileInputOnChange;
  }

  
});

function imageFileInputOnChange(event) {
  let files = event.target.files;
  if (files.length == 0) {
    return;
  }
  let url = "/blog/api/postimages";
  let formData = new FormData();
  formData.append("section", event.target.dataset.section);
  formData.append("blogId", "kmaebashiblog");
  for (let i = 0; i < files.length; i++) {
    formData.append("file" + i, files[i]);
  }
  let req = new Request(url, {
      body: formData,
      method: "POST",
      mode: "no-cors"
    });
    fetch(req)
      .then((res) => console.log(res.status, res.statusText))
      .catch(console.warn);
}
