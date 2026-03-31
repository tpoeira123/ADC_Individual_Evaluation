async function apiCall(path, json, display) {
    try {
        const response = await fetch(path, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(json),
        });
        const answer = await response.json();

        if (display.timeoutId) {
            clearTimeout(display.timeoutId);
        }

        if (answer.status === 'success') {
            if (answer.data.message) {
                display.innerHTML = answer.data.message;
            } else {
                display.innerHTML = JSON.stringify(answer.data);
            }
            display.style.color = "green";
        } else {
            display.innerHTML = "Error: " + answer.data;
            display.style.color = "red";
        }

        display.timeoutId = setTimeout(() => {
            display.innerHTML = "";
        }, 4500);

        return answer;
    } catch (error) {
        if (display.timeoutId) {
            clearTimeout(display.timeoutId);
        }
        display.innerHTML = "Network Error";
        display.style.color = "red";

        display.timeoutId = setTimeout(() => {
            display.innerHTML = "";
        }, 4500);
    }
}


async function createUser() {
    const username = document.getElementById("create_username").value;
    const password = document.getElementById("create_pwd").value;
    const confirmPassword = document.getElementById("create_confirmation").value;
    const phone = document.getElementById("create_phone").value;
    const address = document.getElementById("create_address").value;
    const role = document.getElementById("create_role").value;

    const display = document.getElementById("createUser_output");

    const jsonFormat = {
        input: {
            username: username,
            password: password,
            confirmation: confirmPassword,
            phone: phone,
            address: address,
            role: role
        }
    };
    await apiCall('/rest/createaccount', jsonFormat, display);
}


async function doLogin() {
    const username = document.getElementById("login_username").value;
    const password = document.getElementById("login_pwd").value;

    const display = document.getElementById("login_output")

    const jsonFormat = {
        input: {
            username: username,
            password: password,
        }
    };

    const answer = await apiCall('/rest/login', jsonFormat, display);
    if (answer && answer.status === 'success') {
        display.innerHTML = "Login successful: Token ID: " + answer.data.token.tokenId;
    }
}


async function showUsers() {
    const tokenId = document.getElementById("showUsers_tokenId").value;

    const display = document.getElementById("showUsers_output");

    const jsonFormat = {
        input: {
        },
        token:{
            tokenId: tokenId,
        }
    };
    await apiCall('/rest/showusers', jsonFormat, display);
}


async function deleteUser() {
    const username = document.getElementById("delete_username").value;
    const tokenId = document.getElementById("delete_tokenId").value;

    const display = document.getElementById("delete_output");

    const jsonFormat = {
        input: {
            username: username,
        },
        token:{
            tokenId: tokenId,
        }
    };
    await apiCall('/rest/deleteaccount', jsonFormat, display);

}


async function modifyUser() {
    const username = document.getElementById("modifyUser_username").value;
    const phone = document.getElementById("modifyUser_phone").value;
    const address = document.getElementById("modifyUser_address").value;
    const tokenId = document.getElementById("modifyUser_tokenId").value;

    const display = document.getElementById("modifyUser_output");

    const jsonFormat = {
        input: {
            username: username,
            attributes:{
                phone: phone,
                address: address,
            }
        },
        token:{
            tokenId: tokenId,
        }
    }
    await apiCall('/rest/modaccount', jsonFormat, display);
}


async function showSessions() {
    const tokenId = document.getElementById("showSessions_tokenId").value;

    const display = document.getElementById("showSessions_output");

    const jsonFormat = {
        input: {
        },
        token:{
            tokenId: tokenId,
        }
    };
    await apiCall('/rest/showauthsessions', jsonFormat, display);
}


async function checkRole() {
    const username = document.getElementById("checkRole_username").value;
    const tokenId = document.getElementById("checkRole_tokenId").value;

    const display = document.getElementById("checkRole_output");

    const jsonFormat = {
        input: {
            username: username
        },
        token:{
            tokenId: tokenId,
        }
    };

    await apiCall('/rest/showuserrole', jsonFormat, display);
}


async function changeRole() {
    const username = document.getElementById("changeRole_username").value;
    const newRole = document.getElementById("changeRole_role").value;
    const tokenId = document.getElementById("changeRole_tokenId").value;

    const display = document.getElementById("changeRole_output");

    const jsonFormat = {
        input: {
            username: username,
            newRole: newRole
        },
        token:{
            tokenId: tokenId,
        }
    };

    await apiCall('/rest/changeuserrole', jsonFormat, display);
}


async function changePassword() {
    const username = document.getElementById("changePassword_username").value;
    const oldPassword = document.getElementById("changePassword_oldPassword").value;
    const newPassword = document.getElementById("changePassword_newPassword").value;
    const tokenId = document.getElementById("changePassword_tokenId").value;

    const display = document.getElementById("changePassword_output");

    const jsonFormat = {
        input: {
            username: username,
            oldPassword: oldPassword,
            newPassword: newPassword
        },
        token:{
            tokenId: tokenId,
        }
    }
    await apiCall('/rest/changeuserpwd', jsonFormat, display);
}


async function doLogout() {
    const username = document.getElementById("logout_username").value;
    const tokenId = document.getElementById("logout_tokenId").value;

    const display = document.getElementById("logout_output");

    const jsonFormat = {
        input: {
            username: username,
        },
        token:{
            tokenId: tokenId,
        }
    };
    await apiCall('/rest/logout', jsonFormat, display);
}