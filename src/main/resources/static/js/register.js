document.addEventListener("DOMContentLoaded", function(){
    console.log("✅ register.js loaded");
    const form = document.getElementById("registerForm");

    form.addEventListener("submit", function(event){
        const password = form.querySelector("input[type='password']").value;

        if(password.length < 6){
            event.preventDefault();
            alert("Password must be at least 6 characters long!");
        }
    });
})