## 20.10.25

'How Do Aircraft Systems Communicate?'. It's been a while since I got a Ben Eater video recommended in my YT feed. Aha! I totally forgot about designing my own 8-bit "Computer" back when I blazed through his breadboard computer videos! 

After seeing his SAP-1 Simple As Possible computer come to life, I too wanted to learn how to make one on my own. Cut to today, when I once again revisited his videos -- and most importantly -- found another great resource for learning this stuff: Sebastian Lague. I followed him from back when he participated in Ludum Dare jams and have ever since.

Lague's videos on his Logic Simulator and his process of building different components of a computer were frankly one of the best on the internet. Even though I knew most of what he explained from Eater, his "why" approach where he first comes up with the easiest solutions and refines it so as to make us understand the reasons better, were so satisfying to watch -- he really has a knack for teaching stuff so effortlessly. 

Seeing his simulator work, I wondered how much work would it take to make something like that in C with just SDL/raylib. So I setup a hello-world raylib project with a 2D camera and some zooming/panning. Tomorrow I'll work on the graphics side for a bit before starting on the main simulator code. 

## 18.10.25

When I distro-hopped from Arch to NixOS about two months ago, for fun I also changed my WM from openbox to qtile. I last used a tiling WM about two years ago (dwm from suckless) and migrated to openbox then 'cuz I didn't appreciate the philosophy. I've now used qtile for about two months, and I'm going back to openbox.

Why? tmux. I mainly switched to qtile because I needed a quick way to position my terminals without having to use the mouse again and again. Now that I'm multiplexing terminals under a single window, I frankly have no use for a tiling WM any more. 

Why didn't I use tmux before? I did. Roughly about ~3-4 years ago. I just dipped my toes in to see what's so special. But at that time I was using Emacs as my main editor and most of my workflow didn't require a separate terminal -- Emacs had it all (compiling, gitting, searching, etc). Now that I use vim (after a brief stunt using my own editor), I require 2-4 terminals at any time so using tmux is a no-brainer.

Of course, I also changed some things along with the WM. qtile had it's own status bar and openbox does not, so I reinstalled tint2. For notifications, I use dunst with notify-send (libnotify). 

There is a subtle bug where when I download something from firefox and click the button to open the containing folder, it opens the terminal emulator, not the file manager (PCManFM). Yet when I start the file manager beforehand and then click the button in firefox, it correcly opens in it. I'll have to look into it.

## 14.10.25

To even begin reading Spivak's Calculus, I need to have a few prerequisites under my belt:

1. Know Pre-algebra, Algebra, Trig, Pre-calculus. Every engineering student has done this at some point. 
2. Calc I, II, III on a surface level—just enough to solve problems, no proofs or derivations or anything. Recommended: Paul's Online Notes or Michel van Biezen's ~13hr YouTube playlist.
3. Proof writing. Recommended: How to Prove It: A Structured Approach by Daniel J. Velleman

Currently I'm working on finishing the 2nd part. Even though I'm in Semester III in my college which involves Calc III, I feel like we weren't taught stuff properly, which is why I'm relying on Paul's notes to get basics of Calc 2 and 3 out of the way so that I can move on to step 3. 

## 13.10.25

Our college student association planned a trip to Visapur, Lonavala for a hike to the Visapur Fort. I came back from the trip last night. It was a pretty cool 2-day excursion considering we have our endsems next month. Last I went hiking on a mountain was probably 7 years ago, so I was pretty certain I'd be half dead from exhaustion after the hike; but it wasn't so bad. 

![](20251013001.jpg)

## 29.9.25

On the first few lessons from the Udemy piano course. Before this I could play melodies with single hand without movement. Now the first full lesson incorporates some bass notes too. What helped me was to individually practice both hands until I'm fairly confident that I could play them without thinking too much. Then after however many tries, playing with both hands should be much easier.

I'm also starting to get a hang of which notes are which on the keyboard. The accidentals are not yet introduced in the course, so I'm gonna hold off till later on that. 

Pitch recognition by ear is tricky. Some say it's given to you by birth, while others assert it can be taught just like any other skill. I think it's a mix of both. All the "pitch perfect" musicians I've come across have done some music in their childhood, that helped them develop this "natural-born" talent gradually.

One of the ways pitch recognition can be done without being born with it, is to link a note with the first note of any song. For example, Hans Zimmer's No Time To Caution has a memorable `A` `B` `C` `D` melodic arrangement, which can be used to recognize other similar notes. 

Also, about a week ago I came across a really cool vector calculus problem solution, thought I'd share it here:

$$\text{Prove that }\nabla^2 f(r) = \frac{d^2 f}{dr^2} + \frac{2}{r} \frac{df}{dr}$$

Conventions used: \(\vec{r} = \) vector and \(r = \) magnitude of \(\vec{r}\).

$$\begin{align*}
y &= \text{LHS}\\ 
  &= \nabla^2 f(r)\\
  &= \nabla \cdot \nabla(f(r))\\
  &= \nabla \cdot [f'(r)\nabla(r)]\\
  &= \nabla \cdot \left[f'(r)\frac{\vec{r}}{r}\right]\\
  &= f'(r)\nabla \cdot \left[\frac{\vec{r}}{r}\right] + \frac{\vec{r}}{r}\cdot
     \nabla f'(r)\\
  &= f'(r)\frac{r\nabla \cdot \vec{r} - \vec{r}\cdot \nabla r}{r^2} \\
  &\quad+ \frac{\vec{r}}{r} \cdot \left( f''(r)\frac{\vec{r}}{r} \right)
\end{align*}$$

Same as the first step:

$$\begin{align*}
y &= f'(r) \frac{r \cdot 3 - \vec{r} \cdot \frac{\vec{r}}{r}}{r^2} 
    + \frac{r^2}{r^2} f''(r)\\
  &= f'(r) \cdot 2 \frac{r}{r^2} + f''(r)\\
  &= f''(r) + \frac{2}{r} f'(r)\\
  &= \text{RHS}
\end{align*}$$

QED.

