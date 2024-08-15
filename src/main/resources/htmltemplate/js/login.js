"use strict";

const form = document.getElementById("login-form");
const loginButton = document.getElementById("login-button");

loginButton.onclick = function(e) {
  const urlSearchParams = new URLSearchParams();
  urlSearchParams.append("userid", form.userid.value);
  urlSearchParams.append("password", form.password.value);
  const opt = {method: "POST", body: urlSearchParams};
  fetch("api/checkpassword", opt)
    .then((result) => result.text())
    .then((text) => {
      if (text === "OK") {
        form.submit();
      } else {
        alert("パスワードが違います。");
      }
    });
}

