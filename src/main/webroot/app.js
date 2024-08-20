function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');
    const timeString = `${hours}:${minutes}:${seconds}`;
    document.getElementById('clock').innerText = timeString;
}

setInterval(updateClock, 1000);
updateClock();

function loadGetAllUsersMsg(){
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
            document.getElementById("getresallusers").innerHTML =
            this.responseText;
        }
    xhttp.open("GET", "/api/user");
    xhttp.send();
}

function loadGetMsg() {
    let nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        document.getElementById("getrespmsg").innerHTML =
        this.responseText;
    }
    xhttp.open("GET", "/api/user?name="+nameVar);
    xhttp.send();
}

function loadPostMsg(name, email){
    let url = "/api/user?name=" + name.value+"&email=" +email.value;

    fetch (url, {method: 'POST'})
        .then(x => x.text())
        .then(y => document.getElementById("postrespmsg").innerHTML = y);
}