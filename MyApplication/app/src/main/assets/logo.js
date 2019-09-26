getDataByTagClassName();
function getDataByTagClassName() {
	var inputs = document.getElementsByClassName("slogo-shopname");
alert(inputs[0].getElementsByTagName("strong")[0].innerText);
}