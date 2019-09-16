ClickButtonForFavor();
function ClickButtonForFavor()
{
    var a = document.getElementById("J_TabBar").getElementsByTagName("li");
    var b = a[1];
    c = b.getElementsByTagName("a");
    javascript:android.showPJ(c[0].href);
    c[0].click();
}