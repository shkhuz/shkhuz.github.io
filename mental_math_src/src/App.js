import React, { useState, useEffect } from 'react';
import { FaGear } from "react-icons/fa6";
import './App.css';

function encloseWithParenIfNeg(number) {
    if (number < 0) {
        return '(' + number + ')';
    }
    return number;
}

function Question(props) {
    const arg1 = encloseWithParenIfNeg(props.arg1);
    const arg2 = encloseWithParenIfNeg(props.arg2);
    return <p className="question noselect">{arg1} {props.op} {arg2} = <span className="input">{props.input}</span></p>;
}

function Button(props) {
    const handleClick = () => {
        props.onClick(props.name);
    };

    return (
        <button className={props.className} onClick={handleClick}>{props.name}</button>
    );
}

function isNumeric(value) {
    return /^\d+$/.test(value);
}

function getRandomNumber(from, toIncluding) {
    const range = toIncluding - from + 1;
    const num = Math.floor(Math.floor(Math.random() * range) + from);
    return num; 
}

function getRandomNegPosNumberWithMag(magnitude) {
    return getRandomNumber(-magnitude, magnitude);
}

function getRandomPosNumberWithMag(magnitude) {
    return getRandomNumber(0, magnitude);
}

function getRandomOp(settings) {
    const rand = settings.onlyMul ? 1 : getRandomNumber(1, 3);
    var op = "";
    switch (rand) {
        case 1: op = '*'; break;
        case 2: op = '+'; break;
        case 3: op = '-'; break;
        default: throw new Error("Cannot get random op");
    }
    return op;
}

function getRandomQuestion(settings) {
    const op = getRandomOp(settings);
    var first;
    var second;

    var randFunc = settings.negNums 
        ? getRandomNegPosNumberWithMag
        : getRandomPosNumberWithMag;
    if (op === '*') {
        first = randFunc(20);
        second = randFunc(10);
        var tmp;
        if (getRandomNumber(1, 100) > 50) {
            tmp = first;
            first = second;
            second = tmp;
        }
    } else {
        first = randFunc(20);
        second = randFunc(20);
    }   

    console.log("Generated %d %s %d with " + JSON.stringify(settings), first, op, second);
    return { first, op, second};
}

function isTouchDevice() {
  return (('ontouchstart' in window) ||
     (navigator.maxTouchPoints > 0) ||
     (navigator.msMaxTouchPoints > 0));
}

function getSettingsFromLocalStorage() {
    var settings = {
        onlyMul: false,
        negNums: false,
    };

    const asString = localStorage.getItem("settings");
    const asValue = JSON.parse(asString);
    if (asValue) {
        settings = asValue;
    }
    return settings;
}

function App() {
    const [settings, setSettings] = useState(getSettingsFromLocalStorage());
    
    const [{
        first, 
        op, 
        second,
    }, setQuestion] = useState(() => getRandomQuestion(settings));

    const [input, setInput] = useState("");
    const [numDone, setNumDone] = useState(0);
    const [numIncorrect, setNumIncorrect] = useState(0);
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const handleTouch = (ev) => {
        var changedTouch = ev.changedTouches[0];
        var elem = document
            .elementFromPoint(changedTouch.clientX, changedTouch.clientY);
        if (elem !== ev.target) return;
        if (ev.target.classList.contains("custom-btn")) {
            handleKeypadButton(ev.target.innerHTML);
        }
    };

    const handleMouseup = (ev) => {
        if (ev.target.classList.contains("custom-btn")) {
            handleKeypadButton(ev.target.innerHTML);
        }
    }

    const handleKeypadButton = (name) => {
        var newInput = input;
        if (isNumeric(name)) {
            newInput = input + name;
        } else if (name === '-') {
            if (input.startsWith('-')) {
                newInput = input.substring(1, input.length);
            } else {
                newInput = '-' + input;
            }
        } else if (name === '<') {
            newInput = input.substring(0, input.length - 1);
        } else if (name === 'C') {
            newInput = "";
        }
        setInput(newInput);
        
        var inputNumber = parseInt(newInput, 10);
        var result;
        switch (op) {
            case '+': result = first + second; break;
            case '-': result = first - second; break;
            case '*': result = first * second; break;
            default: throw new Error("Unknown op");
        }
        if (result === inputNumber) {
            setQuestion(getRandomQuestion(settings));
            setInput("");
            setNumDone((prev) => prev + 1);
        } else if (newInput.length === String(result).length) {
            setNumIncorrect((prev) => prev + 1); 
        }
    };
    
    const toggleSidebar = () => {
        if (sidebarOpen) {
            document.getElementById("sidebar").style.width = "0";
        } else {
            document.getElementById("sidebar").style.width = "var(--sidebar-width)";
        }
        setSidebarOpen((prev) => !prev);
    }

    const updateSettings = (name) => {
        const newSettings = settings;
        newSettings[name] = !settings[name];

        setSettings(newSettings);
        localStorage.setItem("settings", JSON.stringify(newSettings));
        setQuestion(getRandomQuestion(newSettings));
        setInput("");
    }

    return (
        <div className="main-ui">
            <div id="header">
                <div className="cur-pointer noclickhighlight open-sidebar" onClick={toggleSidebar}><FaGear /></div>
                <p className="header-middle noselect">{numDone} completed / {numIncorrect} incorrect</p>
            </div>
            <div className="body">
                <div id="sidebar">
                    <div id="sidebar-menu">
                        <ToggleSwitch name="Multiplication Only" checked={settings.onlyMul} setState={() => updateSettings("onlyMul")} />
                        <ToggleSwitch name="Negative Numbers" checked={settings.negNums} setState={() => updateSettings("negNums")} />
                    </div>
                </div>
                <Question arg1={first} op={op} arg2={second} input={input} />
                <div className="keypad">
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="1" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="2" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="3" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="4" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="5" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="6" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="7" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="8" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="9" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="-" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} name="0" />
                    <CustomButton onMouseUp={handleMouseup} onTouchEnd={handleTouch} className="C" name="C" />
                </div>
            </div>
        </div>
    );
}

function ToggleSwitch(props) {
    return (
        <div className="noselect toggle-container">
            <div className="toggle-label cur-pointer noclickhighlight" onClick={props.setState}>{props.name}</div>
            <label className="cur-pointer toggle-switch noclickhighlight">
                <input className="cur-pointer" type="checkbox" checked={props.checked} onChange={props.setState} />
                <span className="slider round"></span>
            </label>
        </div>
    );
}

function CustomButton(props) {
    return <div onMouseUp={!isTouchDevice() ? props.onMouseUp : undefined} onTouchEnd={props.onTouchEnd} className={(props.className ? props.className : "") + " custom-btn cur-pointer noselect noclickhighlight"}>{props.name}</div>
}

export default App;
