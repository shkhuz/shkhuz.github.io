import React, { useState } from 'react';
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
    const rand = getRandomNumber(3);
    var op = "";
    switch (rand) {
        case 1: op = '+'; break;
        case 2: op = '-'; break;
        case 3: op = '*'; break;
        default: throw new Error("Cannot get random op");
    }
    return op;
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

function App() {
    const [{
        first, 
        op, 
        second,
    }, setQuestion] = useState(getRandomQuestion());
    const [input, setInput] = useState("");
    const [numDone, setNumDone] = useState(0);
    const [numIncorrect, setNumIncorrect] = useState(0);

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

    return (
        <div className="main-ui">
            <div className="header">
                <p>{numDone} completed / {numIncorrect} incorrect</p>
            </div>
            <Question arg1={first} op={op} arg2={second} input={input} />
            <div className="keypad">
                <Button name="1" onClick={handleKeypadButton} />
                <Button name="2" onClick={handleKeypadButton} />
                <Button name="3" onClick={handleKeypadButton} />
                <Button name="4" onClick={handleKeypadButton} />
                <Button name="5" onClick={handleKeypadButton} />
                <Button name="6" onClick={handleKeypadButton} />
                <Button name="7" onClick={handleKeypadButton} />
                <Button name="8" onClick={handleKeypadButton} />
                <Button name="9" onClick={handleKeypadButton} />
                <Button name="-" onClick={handleKeypadButton} />
                <Button name="0" onClick={handleKeypadButton} />
                <Button name="C" onClick={handleKeypadButton} className="C" />
            </div>
        </div>
    );
}

export default App;
