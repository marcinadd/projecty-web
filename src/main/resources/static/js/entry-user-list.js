addSetOnClickListener();

function addSetOnClickListener() {
    var add = document.getElementById("add");
    console.log("Ok");
    add.onclick = addUserEntry

}

var counter = 0;

function addUserEntry() {
    var users = document.getElementById("users");
    var newUser = document.createElement("input");
    newUser.setAttribute("type", "text");
    newUser.setAttribute("class", "form-control");
    newUser.setAttribute("name", "usernames");
    newUser.setAttribute("id", counter.toString());
    counter++;
    users.appendChild(newUser);
}