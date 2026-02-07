"use strict";

const PageType = {
  TOP: "TOP",
  POST: "POST",
  DATE: "DATE"
}
Object.freeze(PageType);

let currentPageType;

window.onload = function() {
  const propElem = document.getElementById("properties");
  const date = parseDateYYYYMMDD(propElem.dataset.postedDate);
  const calendarElem = document.getElementById("calendar-area");
  const calendar = new Calendar(calendarElem, date, propElem.dataset.pageType);

  const searchButton = document.getElementById("search-button-sidebar");
  searchButton.onclick = sidebarSearchClicked;
}

function parseDateYYYYMMDD(str) {
  const year = parseInt(str.substring(0, 4));
  const month = parseInt(str.substring(4, 6)) - 1;
  const day = parseInt(str.substring(6, 8));

  return new Date(year, month, day);
}

function sidebarSearchClicked(e) {
  const parentDiv = document.getElementById("sidebar-search-area");

  redirectToSearchPage(parentDiv);
}

function redirectToSearchPage(searchAreaDiv) {
  const textInput = searchAreaDiv.getElementsByClassName("search-input")[0];
  const keywords = textInput.value;

  const checkAreaDiv = searchAreaDiv.getElementsByClassName("search-check-area")[0];
  const titleSearch = checkAreaDiv.getElementsByClassName("search-title-check")[0].checked;
  const contentSearch = checkAreaDiv.getElementsByClassName("search-content-check")[0].checked;

  var mode;
  if (titleSearch && !contentSearch) {
    mode = "title";
  } else if (!titleSearch && contentSearch) {
    mode = "content";
  } else {
    mode = "both";
  }
  const pageType = document.getElementById("properties").dataset.pageType;
  let baseUrl;
  if (pageType === "TOP") {
    const match = window.location.pathname.match(/^\/\w+\/(\w+)$/);
    baseUrl = "./" + match[1] + "/";
  } else if (pageType === "POST") {
    baseUrl = "../";
  } else if (pageType === "DATE") {
    baseUrl = "./";
  }
  const params = new URLSearchParams({
    q: keywords,
    mode: mode
  });
  const url = baseUrl + "searchlist?" + params;
  console.log("url.." + url);

  location.href = url;
}
