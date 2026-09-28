/* =====================================================
   MENTORCONNECT LOGIN
   ===================================================== */


/* Current selected role */

let selectedRole = "student";


/* ===============================
   SELECT ROLE
================================ */

function selectRole(role) {

    selectedRole = role;

    const slider =
        document.getElementById("roleSlider");

    const studentButton =
        document.getElementById("studentRoleBtn");

    const mentorButton =
        document.getElementById("mentorRoleBtn");

    const description =
        document.getElementById("roleDescription");

    const loginButtonText =
        document.getElementById("loginButtonText");


    if (role === "student") {

        slider.classList.remove("mentor");

        studentButton.classList.add("active");

        mentorButton.classList.remove("active");

        description.classList.remove("mentor");

        description.innerHTML = `
            <span>🎓</span>

            <div>

                <b>
                    Student Portal
                </b>

                <small>
                    Find mentors based on your interests,
                    skills and career goals.
                </small>

            </div>
        `;

        loginButtonText.textContent =
            "Enter Student Portal";

    }


    else {

        slider.classList.add("mentor");

        studentButton.classList.remove("active");

        mentorButton.classList.add("active");

        description.classList.add("mentor");

        description.innerHTML = `
            <span>👨‍🏫</span>

            <div>

                <b>
                    Mentor Portal
                </b>

                <small>
                    Guide students, manage your expertise
                    and track mentorship sessions.
                </small>

            </div>
        `;

        loginButtonText.textContent =
            "Enter Mentor Portal";

    }

}


/* ===============================
   TOUCH SWIPE
================================ */

let touchStartX = 0;

let touchEndX = 0;


const loginCard =
    document.querySelector(".login-card");


if (loginCard) {

    loginCard.addEventListener(
        "touchstart",
        function(event) {

            touchStartX =
                event.changedTouches[0].screenX;

        },
        {
            passive: true
        }
    );


    loginCard.addEventListener(
        "touchend",
        function(event) {

            touchEndX =
                event.changedTouches[0].screenX;

            handleSwipe();

        },
        {
            passive: true
        }
    );

}


function handleSwipe() {

    const difference =
        touchEndX - touchStartX;


    /*
       Swipe left
       Student -> Mentor
    */

    if (
        difference < -60 &&
        selectedRole === "student"
    ) {

        selectRole("mentor");

    }


    /*
       Swipe right
       Mentor -> Student
    */

    else if (
        difference > 60 &&
        selectedRole === "mentor"
    ) {

        selectRole("student");

    }

}


/* ===============================
   DESKTOP MOUSE DRAG
================================ */

let mouseStartX = 0;


if (loginCard) {

    loginCard.addEventListener(
        "mousedown",
        function(event) {

            mouseStartX = event.clientX;

        }
    );


    loginCard.addEventListener(
        "mouseup",
        function(event) {

            const difference =
                event.clientX - mouseStartX;


            if (
                difference < -60 &&
                selectedRole === "student"
            ) {

                selectRole("mentor");

            }

            else if (
                difference > 60 &&
                selectedRole === "mentor"
            ) {

                selectRole("student");

            }

        }
    );

}


/* ===============================
   PASSWORD VISIBILITY
================================ */

function togglePassword() {

    const password =
        document.getElementById("loginPassword");


    if (password.type === "password") {

        password.type = "text";

    }

    else {

        password.type = "password";

    }

}


/* ===============================
   LOGIN FORM
================================ */

const loginForm =
    document.getElementById("loginForm");


if (loginForm) {

    loginForm.addEventListener(
        "submit",
        function(event) {

            event.preventDefault();

            handleLogin();

        }
    );

}


/* ===============================
   LOGIN
================================ */

function handleLogin() {

    const identity =
        document
            .getElementById("loginIdentity")
            .value
            .trim();


    const password =
        document
            .getElementById("loginPassword")
            .value
            .trim();


    if (!identity || !password) {

        showMessage(
            "Please enter your login details."
        );

        return;

    }


    /*
       Temporary frontend login.

       Replace this section with:

       fetch("http://localhost:8081/api/auth/login")

       when your Spring Boot authentication
       endpoint is available.
    */


    const user = {

        identity: identity,

        role: selectedRole

    };


    sessionStorage.setItem(
        "mentorConnectUser",
        JSON.stringify(user)
    );


    openApplication(user);

}


/* ===============================
   DEMO LOGIN
================================ */

function demoLogin(role) {

    selectRole(role);


    const user = {

        identity:
            role === "student"
                ? "demo.student"
                : "demo.mentor",

        role: role,

        demo: true

    };


    sessionStorage.setItem(
        "mentorConnectUser",
        JSON.stringify(user)
    );


    setTimeout(
        function() {

            openApplication(user);

        },
        400
    );

}


/* ===============================
   OPEN APPLICATION
================================ */

function openApplication(user) {

    const loginScreen =
        document.getElementById("loginScreen");

    const application =
        document.getElementById("application");

    const welcomeUser =
        document.getElementById("welcomeUser");


    loginScreen.style.display =
        "none";


    application.classList.remove(
        "hidden"
    );


    if (user.role === "student") {

        welcomeUser.innerHTML = `
            🎓 Welcome, Student!
            <br>
            <small>
                Your mentorship journey starts here.
            </small>
        `;

    }

    else {

        welcomeUser.innerHTML = `
            👨‍🏫 Welcome, Mentor!
            <br>
            <small>
                Ready to guide the next generation?
            </small>
        `;

    }

}


/* ===============================
   LOGOUT
================================ */

function logout() {

    sessionStorage.removeItem(
        "mentorConnectUser"
    );


    document
        .getElementById("application")
        .classList.add("hidden");


    document
        .getElementById("loginScreen")
        .style.display = "flex";


    document
        .getElementById("loginForm")
        .reset();


    selectRole("student");

}


/* ===============================
   TOAST
================================ */

function showMessage(message) {

    const toast =
        document.getElementById("toast");


    toast.textContent =
        message;


    toast.classList.add("show");


    setTimeout(
        function() {

            toast.classList.remove("show");

        },
        3000
    );

}


/* ===============================
   RESTORE SESSION
================================ */

window.addEventListener(
    "DOMContentLoaded",
    function() {

        const savedUser =
            sessionStorage.getItem(
                "mentorConnectUser"
            );


        if (savedUser) {

            try {

                const user =
                    JSON.parse(savedUser);

                selectedRole =
                    user.role;

                openApplication(user);

            }

            catch (error) {

                sessionStorage.removeItem(
                    "mentorConnectUser"
                );

            }

        }

    }
);