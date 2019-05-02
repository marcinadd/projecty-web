addSetOnClickListener();
function addSetOnClickListener() {
    var add = document.getElementById("add");
    console.log("Ok");
    add.onclick = addUserEntry;

    var remove=document.getElementById("remove");
    console.log("Okr");
    remove.onclick = removeUserEntry;

}

var counter = 0;
var prefix="userEntry";


function addUserEntry() {
    var users = document.getElementById("users");
    var newUser = document.createElement("input");
    newUser.setAttribute("type", "text");
    newUser.setAttribute("class", "form-control");
    newUser.setAttribute("name", "usernames");
    counter++;
    newUser.setAttribute("id", prefix + counter.toString());
    users.appendChild(newUser);
}

function removeUserEntry() {
    if (counter>0){
        var entry = document.getElementById(prefix+counter);
        console.log("OKKKKKK");
        entry.parentNode.removeChild(entry);
        counter--;
    }
}