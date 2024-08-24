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
}

function parseDateYYYYMMDD(str) {
  const year = parseInt(str.substring(0, 4));
  const month = parseInt(str.substring(4, 6)) - 1;
  const day = parseInt(str.substring(6, 8));

  return new Date(year, month, day);
}

