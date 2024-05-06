import React, { useState, useEffect } from 'react';
import { FaGear } from "react-icons/fa6";
import { IoClose } from "react-icons/io5";
import './App.css';

function Question(props) {
    return <p className="question">{props.arg1} {props.op} {props.arg2} = <span className="input">{props.input}</span></p>;
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

function getRandomNumber(including) {
    return Math.floor(Math.floor(Math.random() * including) + 1);
}

function getRandomOp() {
    // const rand = getRandomNumber(3);
    // var op = "";
    // switch (rand) {
    //     case 1: op = '+'; break;
    //     case 2: op = '-'; break;
    //     case 3: op = '*'; break;
    //     default: throw new Error("Cannot get random op");
    // }
    // return op;
    return '*';
}

function getRandomQuestion() {
    const op = getRandomOp();
    var first;
    var second;

    if (op === '*') {
        first = getRandomNumber(20);
        second = getRandomNumber(10);
        var tmp;
        if (getRandomNumber(100) > 50) {
            tmp = first;
            first = second;
            second = tmp;
        }
    } else {
        first = getRandomNumber(20);
        second = getRandomNumber(20);
    }   

    return { first, op, second};
}

function isTouchDevice() {
  return (('ontouchstart' in window) ||
     (navigator.maxTouchPoints > 0) ||
     (navigator.msMaxTouchPoints > 0));
}

function App() {
    const [{
        first, 
        op, 
        second,
    }, setQuestion] = useState(getRandomQuestion());

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
            setQuestion(getRandomQuestion());
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
            document.getElementById("sidebar").style.width = "100%";
        }
        setSidebarOpen((prev) => !prev);
    }

    return (
        <div className="main-ui">
            <div id="header">
                <div className="open-sidebar" onClick={toggleSidebar}><FaGear /></div>
                <p className="header-middle noselect">{numDone} completed / {numIncorrect} incorrect</p>
            </div>
            <div className="body">
                <div id="sidebar">
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

function CustomButton(props) {
    return <div onMouseUp={!isTouchDevice() ? props.onMouseUp : undefined} onTouchEnd={props.onTouchEnd} className={(props.className ? props.className : "") + " custom-btn noselect"}>{props.name}</div>
}

export default App;
