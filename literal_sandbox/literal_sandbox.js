function getOffset(el) {
    const rect = el.getBoundingClientRect();
    return {
        left: rect.left + window.scrollX,
        top: rect.top + window.scrollY,
    };
}

const cv = document.getElementById("main");
const cc = cv.getContext("2d");
const cvoff = getOffset(cv);
cv.width  = cv.offsetWidth;
cv.height = cv.offsetHeight;

var lmb_down = false;
var mouseX, mouseY;
var updateFpsInterval, renderFpsInterval, startTime, now, updateThen, renderThen, elapsed;

const blobRadStates = [3, 5, 7, 9];
var curBlobRadState = 0;
const forceStates = [0, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1, 2, 3, 5, 10];
var curForceState = 0;

document.getElementById("force-btn").textContent = "Force: " + forceStates[curForceState].toString();

function updateMousePosByEvent(ev) {
    if (ev.type == 'touchstart' || ev.type == 'touchmove' || ev.type == 'touchend' || ev.type == 'touchcancel') {
        const { touches, changedTouches } = ev.originalEvent ?? ev;
        const touch = touches[0] ?? changedTouches[0];
        mouseX = touch.pageX;
        mouseY = touch.pageY;
    } else if (ev.type == 'mousedown' || ev.type == 'mouseup' || ev.type == 'mousemove' || ev.type == 'mouseover'|| ev.type=='mouseout' || ev.type=='mouseenter' || ev.type=='mouseleave') {
        mouseX = ev.clientX;
        mouseY = ev.clientY;
    }
}

class Grid {
    constructor(rows) {
        this.rows = rows;
        this.dim = cv.height / rows;
        this.cols = Math.floor(cv.width / this.dim);

        this.elems = Array(this.rows * this.cols);
        for (let i = 0; i < this.rows*this.cols; i++) {
            this.elems[i] = {val: 0, acc: 1};
        }
        this.elems[5*this.cols + 5].val = 1;
    }

    mouseDragOrDown(ev) {
    }

    mouseDrag(ev) {
        this.mouseDragOrDown(ev);
    }

    mouseDown(ev) {
        this.mouseDragOrDown(ev);
    }

    idx(r, c) {
        return r*this.cols + c;
    }

    acc(r, c) {
        if (r >= 0 && r <= this.rows-1 &&
            c >= 0 && c <= this.cols-1) {
            return this.elems[this.idx(r, c)];
        } else return null;
    }

    accval(r, c) {
        const a = this.acc(r, c);
        if (a) return a.val;
        else return -1;
    }

    accacc(r, c) {
        const a = this.acc(r, c);
        if (a) return a.acc;
        else return -1;
    }

    setval(r, c, val) {
        if (r >= 0 && r <= this.rows-1 &&
            c >= 0 && c <= this.cols-1) {
            this.elems[this.idx(r, c)].val = val;
        }
    }

    setacc(r, c, acc) {
        if (r >= 0 && r <= this.rows-1 &&
            c >= 0 && c <= this.cols-1) {
            this.elems[this.idx(r, c)].acc = acc;
        }
    }

    update() {
        if (lmb_down) {
            if (mouseX >= cvoff.left && mouseY >= cvoff.top) {
                const x = mouseX - cvoff.left;
                const y = mouseY - cvoff.top;
                const col = Math.floor(x / this.dim);
                const row = Math.floor(y / this.dim);
                if (row >= 0 && row <= this.rows-1 &&
                    col >= 0 && col <= this.cols-1) {
                    const halfRad = Math.floor(blobRadStates[curBlobRadState] * 0.5);
                    for (let i = -halfRad; i <= halfRad; i++) {
                        for (let j = -halfRad; j <= halfRad; j++) {
                            if (Math.random() > 0.5) {
                                this.setval(row+i, col+j, 1);
                            }
                        }
                    }
                }
            }
        }

        for (let r = this.rows-1; r >= 0; r--) {
            for (let c = this.cols-1; c >= 0; c--) {
                if (this.accval(r, c) == 1) {
                    var canGoDown = 0;
                    for (let i = 1; i <= this.accacc(r, c); i++) {
                        if (this.accval(r+i, c) == 0) {
                            canGoDown++;
                        } else break;
                    }
                   
                    if (canGoDown) {
                        this.setval(r+canGoDown, c, 1);
                        this.setacc(r+canGoDown, c, this.accacc(r, c)+forceStates[curForceState]);
                        this.setval(r, c, 0);
                        this.setacc(r, c, 1);
                    } else {
                        const dir = Math.random() > 0.5 ? -1 : 1;
                        if (this.accval(r+1, c+dir) == 0) {
                            this.setval(r, c, 0);
                            this.setval(r+1, c+dir, 1);
                        } else if (this.accval(r+1, c-dir) == 0) {
                            this.setval(r, c, 0);
                            this.setval(r+1, c-dir, 1);
                        } else {
                            this.setacc(r, c, 1);
                        }
                    }
                }
            }
        }
    }

    render() {
        cc.strokeStyle = "white";
        cc.fillStyle = "white";
        cc.beginPath();
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if (this.accval(r, c) !== 0) {
                    cc.rect(c*this.dim, r*this.dim, this.dim, this.dim);
                }
            }
        }
        cc.fill();
        cc.closePath();

        // cc.lineWidth = 0.4;
        // for (let r = 1; r < this.rows; r++) {
        //     cc.beginPath();
        //     cc.moveTo(0, r*this.dim);
        //     cc.lineTo(cv.width, r*this.dim);
        //     cc.stroke();
        // }
        // for (let c = 1; c < this.cols; c++) {
        //     cc.beginPath();
        //     cc.moveTo(c*this.dim, 0);
        //     cc.lineTo(c*this.dim, cv.height);
        //     cc.stroke();
        // }
    }
}

var grid = new Grid(200);

window.addEventListener("resize", (ev) => {
    cvoff = getOffset(cv);
    cv.width  = cv.offsetWidth;
    cv.height = cv.offsetHeight;
    grid = new Grid(100);
});

window.addEventListener("mousedown", (ev) => {
    if (ev.which == 1) {
        lmb_down = true;
        updateMousePosByEvent(ev);
    }
});
window.addEventListener("touchstart", (ev) => {
    lmb_down = true;
    updateMousePosByEvent(ev);
});

window.addEventListener("mouseup", (ev) => {
    if (ev.which == 1) lmb_down = false;
    grid.mouseDown(ev);
});
window.addEventListener("touchend", (ev) => {
    lmb_down = false
    grid.mouseDown(ev);
});

window.addEventListener('mousemove', (ev) => updateMousePosByEvent(ev));
window.addEventListener('touchmove', (ev) => updateMousePosByEvent(ev));

function update() {
    grid.update();
} 

function render() {
    grid.render();
}

function each_frame() {
    now = Date.now();
    updateElapsed = now - updateThen;
    if (updateElapsed > updateFpsInterval) {
        updateThen = now - (updateElapsed % updateFpsInterval);
        update();
    }

    renderElapsed = now - renderThen;
    if (renderElapsed > renderFpsInterval) {
        renderThen = now - (renderElapsed % renderFpsInterval);
        cc.clearRect(0, 0, cv.width, cv.height);
        render();
    }
    requestAnimationFrame(each_frame);
}

function start_loop(updateFps, renderFps) {
    updateFpsInterval = 1000 / updateFps;
    renderFpsInterval = 1000 / renderFps;
    updateThen = Date.now();
    renderThen = updateThen;
    startTime = updateThen;
    each_frame();
}

start_loop(60, 60);

function blobBtn() {
    if (curBlobRadState < blobRadStates.length-1) curBlobRadState++;
    else curBlobRadState = 0;
    const blobRad = blobRadStates[curBlobRadState].toString();
    document.getElementById("blob-btn").textContent = "Radius: " + blobRad + 'x' + blobRad;
}

function forceBtn() {
    if (curForceState < forceStates.length-1) curForceState++;
    else curForceState = 0;
    const force = forceStates[curForceState].toString();
    document.getElementById("force-btn").textContent = "Force: " + force;
}
