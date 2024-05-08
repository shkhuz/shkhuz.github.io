const canvas = document.getElementById("main-canvas");
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;
var cc = canvas.getContext("2d");
cc.translate(canvas.width/2, canvas.height/2);

function rand(from, toIncluding) {
    const range = toIncluding - from + 1;
    const num = Math.floor(Math.floor(Math.random() * range) + from);
    return num; 
}

function map(value, a, b, c, d) {
    value = (value-a) / (b-a);
    return c + value*(d-c);
}

class Star {
    constructor() {
        this.x = rand(-canvas.width/2, canvas.width/2);
        this.y = rand(-canvas.height/2, canvas.height/2);
        this.z = rand(0, canvas.width);
        this.pz = this.z;
    }

    update() {
        this.z -= 30;
        if (this.z < 1) {
            this.z = canvas.width;
            this.x = rand(-canvas.width/2, canvas.width/2);
            this.y = rand(-canvas.height/2, canvas.height/2);
            this.pz = this.z;
        }
    }

    draw() {
        cc.fillStyle = "white";
        cc.beginPath();
        const sx = map(this.x/this.z, 0, 1, 0, canvas.width);
        const sy = map(this.y/this.z, 0, 1, 0, canvas.height);
        const r = map(this.z, 0, canvas.width, 5, 0);
        cc.arc(sx, sy, r, 0, 2*Math.PI);
        cc.closePath();
        cc.fill();

        const px = map(this.x/this.pz, 0, 1, 0, canvas.width);
        const py = map(this.y/this.pz, 0, 1, 0, canvas.height);
        this.pz = this.z;

        cc.lineWidth = 1;
        cc.strokeStyle = "white";
        cc.beginPath();
        cc.moveTo(px, py);
        cc.lineTo(sx, sy);
        cc.stroke();
    }
}

var stars = Array(800);
for (let i = 0; i < stars.length; i++) {
    stars[i] = new Star();
}

each_frame();

window.addEventListener("resize", (ev) => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    cc.translate(canvas.width/2, canvas.height/2);
});

function update() {
    for (let i = 0; i < stars.length; i++) {
        stars[i].update();
    }
}

function draw() {
    for (let i = 0; i < stars.length; i++) {
        stars[i].draw();
    }
}

function each_frame() {
    cc.clearRect(-canvas.width/2, -canvas.height/2, canvas.width, canvas.height);

    update();
    draw();

    requestAnimationFrame(each_frame);
}
