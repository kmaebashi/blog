"use strict";

const PageType = {
  TOP: "TOP",
  POST: "POST",
  DATE: "DATE"
}
Object.freeze(PageType);

let currentPageType;

window.onload = function() {
  if (window.location.pathname.match(/^\/\w+\/\w+$/)) {
    currentPageType = PageType.TOP;
  } else if (window.location.pathname.match(/^\/\w+\/\w+\/post\/\d+$/)) {
    currentPageType = PageType.POST;
  } else if (window.location.pathname.match(/^\/\w+\/\w+\/\d\d\d\d\d\d$/)
          || window.location.pathname.match(/^\/\w+\/\w+\/\d\d\d\d\d\d\d\d$/)) {
    currentPageType = PageType.DATE;
  } else {
    console.log("URLが変です");
  }
  const calendarElem = document.getElementById("calendar-area");
  const calendar = new Calendar(calendarElem, new Date());
}

