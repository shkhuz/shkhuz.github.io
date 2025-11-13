# Nix and Electron

Today I got a project from a friend that asked me to make some changes to their Task Management app. It was a simple Electron app, and as much as I hated working with `electron`, I decided to work on it as a favour. To give some context, I had recently installed NixOS on my main laptop, and until now didn't face any major issues installing packages or configuring it for my needs. So, I cloned the repo and ran `npm install` to build the project:

```console
> taskmgr@1.0.0 postinstall
> electron-builder install-app-deps

  • electron-builder  version=24.13.3
  • loaded configuration  file=package.json ("build" field)
  • rebuilding native dependencies  dependencies=bcrypt@5.1.1, sqlite3@5.1.7 platform=linux arch=x64
  • install prebuilt binary  name=sqlite3 version=5.1.7 platform=linux arch=x64 napi=
  • build native dependency from sources  name=sqlite3
                                          version=5.1.7
                                          platform=linux
                                          arch=x64
                                          napi=
reason=prebuild-install failed with error (run with env DEBUG=electron-builder to get more information)
                                          error=prebuild-install info begin Prebuild-install version 7.1.3
    prebuild-install warn This package does not support N-API version 36
    prebuild-install warn install prebuilt binaries enforced with --force!
    prebuild-install warn install prebuilt binaries may be out of date!
    prebuild-install info looking for local prebuild @ prebuilds/sqlite3-v5.1.7-napi-v36-linux-x64.tar.gz
    prebuild-install info looking for cached prebuild @ /home/huzaifa/.npm/_prebuilds/c8064a-sqlite3-v5.1.7-napi-v36-linux-x64.tar.gz
    prebuild-install http request GET https://github.com/TryGhost/node-sqlite3/releases/download/v5.1.7/sqlite3-v5.1.7-napi-v36-linux-x64.tar.gz
    prebuild-install http 404 https://github.com/TryGhost/node-sqlite3/releases/download/v5.1.7/sqlite3-v5.1.7-napi-v36-linux-x64.tar.gz
    prebuild-install warn install No prebuilt binaries found (target=36 runtime=napi arch=x64 libc= platform=linux)
    ...
```

With my lackluster abilities working with Node and NPM, I started to search for a solution for this seemingly innocuous error. After a little back and forth, I found that the `prebuild-install` library (likely a dependency of `electron`) was not able to find a suitable version of `sqlite` on my machine, and as I had `nodejs_24` in my `configuration.nix`, it for some reason wasn't able to find a prebuilt version of it on GitHub. 

```console
  ...
  ⨯ cannot execute  cause=exit status 1
                    errorOut=npm error code 1
    npm error path /home/huzaifa/dev/taskmgr/node_modules/sqlite3
    npm error command failed
    npm error command sh -c prebuild-install -r napi || node-gyp rebuild
    npm error prebuild-install warn This package does not support N-API version 28.3.3
    npm error prebuild-install warn install No prebuilt binaries found (target=28.3.3 runtime=napi arch=x64 libc= platform=linux)
    npm error gyp info it worked if it ends with ok
    npm error gyp info using node-gyp@8.4.1
    npm error gyp info using node@22.17.1 | linux | x64
    npm error (node:26962) [DEP0060] DeprecationWarning: The `util._extend` API is deprecated. Please use Object.assign() instead.
    npm error (Use `node --trace-deprecation ...` to show where the warning was created)
    npm error gyp info find Python using Python version 3.12.11 found at "/usr/bin/python3"
    npm error gyp info spawn /usr/bin/python3
    npm error gyp info spawn args [
    npm error gyp info spawn args   '/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/gyp/gyp_main.py',
    npm error gyp info spawn args   'binding.gyp',
    npm error gyp info spawn args   '-f',
    npm error gyp info spawn args   'make',
    npm error gyp info spawn args   '-I',
    npm error gyp info spawn args   '/home/huzaifa/dev/taskmgr/node_modules/sqlite3/build/config.gypi',
    npm error gyp info spawn args   '-I',
    npm error gyp info spawn args   '/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/addon.gypi',
    npm error gyp info spawn args   '-I',
    npm error gyp info spawn args   '/home/huzaifa/.electron-gyp/28.3.3/include/node/common.gypi',
    npm error gyp info spawn args   '-Dlibrary=shared_library',
    npm error gyp info spawn args   '-Dvisibility=default',
    npm error gyp info spawn args   '-Dnode_root_dir=/home/huzaifa/.electron-gyp/28.3.3',
    npm error gyp info spawn args   '-Dnode_gyp_dir=/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp',
    npm error gyp info spawn args   '-Dnode_lib_file=/home/huzaifa/.electron-gyp/28.3.3/<(target_arch)/node.lib',
    npm error gyp info spawn args   '-Dmodule_root_dir=/home/huzaifa/dev/taskmgr/node_modules/sqlite3',
    npm error gyp info spawn args   '-Dnode_engine=v8',
    npm error gyp info spawn args   '--depth=.',
    npm error gyp info spawn args   '--no-parallel',
    npm error gyp info spawn args   '--generator-output',
    npm error gyp info spawn args   'build',
    npm error gyp info spawn args   '-Goutput_dir=.'
    npm error gyp info spawn args ]
    npm error Traceback (most recent call last):
    npm error   File "/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/gyp/gyp_main.py", line 42, in <module>
    npm error     import gyp  # noqa: E402
    npm error     ^^^^^^^^^^
    npm error   File "/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/gyp/pylib/gyp/__init__.py", line 9, in <module>
    npm error     import gyp.input
    npm error   File "/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/gyp/pylib/gyp/input.py", line 19, in <module>
    npm error     from distutils.version import StrictVersion
    npm error ModuleNotFoundError: No module named 'distutils'
    npm error gyp ERR! configure error
    npm error gyp ERR! stack Error: `gyp` failed with exit code: 1
    npm error gyp ERR! stack     at ChildProcess.onCpExit (/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/node-gyp/lib/configure.js:259:16)
    npm error gyp ERR! stack     at ChildProcess.emit (node:events:518:28)
    npm error gyp ERR! stack     at ChildProcess._handle.onexit (node:internal/child_process:293:12)
    npm error gyp ERR! System Linux 6.12.41
    npm error gyp ERR! command "/nix/store/ijsy113yzy0mpcr1sf0773nz9v0r7hff-nodejs-22.17.1/bin/node" "/home/huzaifa/dev/taskmgr/node_modules/sqlite3/node_modules/.bin/node-gyp" "rebuild"
    npm error gyp ERR! cwd /home/huzaifa/dev/taskmgr/node_modules/sqlite3
    npm error gyp ERR! node -v v22.17.1
    npm error gyp ERR! node-gyp -v v8.4.1
    npm error gyp ERR! not ok
    npm error A complete log of this run can be found in: /home/huzaifa/.npm/_logs/2025-10-27T18_14_32_797Z-debug-0.log

                    command=/nix/store/ijsy113yzy0mpcr1sf0773nz9v0r7hff-nodejs-22.17.1/bin/node /nix/store/ijsy113yzy0mpcr1sf0773nz9v0r7hff-nodejs-22.17.1/lib/node_modules/npm/bin/npm-cli.js rebuild bcrypt@5.1.1 sqlite3@5.1.7
                    workingDir=
npm error code 1
npm error path /home/huzaifa/dev/taskmgr
npm error command failed
npm error command sh -c electron-builder install-app-deps
npm error A complete log of this run can be found in: /home/huzaifa/.npm/_logs/2025-10-27T18_14_26_543Z-debug-0.log
```

Unable to find it online too, it then tries to build `sqlite3` from source locally. I now faced another issue: even building it locally resulted in a failed compilation. Specifically:

```console
    npm error ModuleNotFoundError: No module named 'distutils'
```

I guess the python version I got from nixpkgs didn't come bundled with `distutils`. Searching online, I saw that `distutlils` was a depreciated module (I guess) and wasn't being shipped in newer python versions. As my experience with Nix was somewhat new, installing a specific python version with a specific python module was difficult, as I didn't fully "get" the Nix language just yet. 

Instead of fixing this and wasting time fixing something I won't use again, I searched for a better solution. `better-sqlite3` has prebuilt binaries for many more ABIs than `sqlite3`. So after a quick look at their [https://github.com/WiseLibs/better-sqlite3](GitHub), I decided to go for it. I changed `package.json`:

```diff
  "dependencies": {
-    "sqlite": "^5.1.1",
-    "sqlite3": "^5.1.7",
+    "better-sqlite3": "^9.6.0",
```

To move to `better-sqlite3`, I also had to change all the source files to use its API rather than `sqlite3`'s.

I also deleted `node_modules/`, `~/.electron-gyp` for a fresh start. Doing `npm install`:

```console
npm verbose pkgid better-sqlite3@9.6.0
npm error code 1
npm error path /home/huzaifa/dev/taskmgr/node_modules/better-sqlite3
npm error command failed
npm error command sh -c prebuild-install || node-gyp rebuild --release
npm error prebuild-install info begin Prebuild-install version 7.1.3
npm error prebuild-install info looking for local prebuild @ prebuilds/better-sqlite3-v9.6.0-node-v127-linux-x64.tar.gz
npm error prebuild-install info looking for cached prebuild @ /home/huzaifa/.npm/_prebuilds/26e7c7-better-sqlite3-v9.6.0-node-v127-linux-x64.tar.gz
npm error prebuild-install http request GET https://github.com/WiseLibs/better-sqlite3/releases/download/v9.6.0/better-sqlite3-v9.6.0-node-v127-linux-x64.tar.gz
npm error prebuild-install http 404 https://github.com/WiseLibs/better-sqlite3/releases/download/v9.6.0/better-sqlite3-v9.6.0-node-v127-linux-x64.tar.gz
npm error prebuild-install warn install No prebuilt binaries found (target=22.17.1 runtime=node arch=x64 libc= platform=linux)
```

Ohh. Scrolling up:

```console*
...
// hlt-start
npm info using npm@10.9.2
npm info using node@v22.17.1
npm verbose title npm install
// hlt-end
...
```

After fiddling around for a bit, I found out that while `better-sqlite` has prebuilt binaries for more targets that `sqlite3`, it still doesn't suppport the latest `node` version. I figured the only way to solve this issue would be to downgrade `node` itself. So I did just that. `configuration.nix` got a `nodejs_20` and after `nrs` (`nixos-rebuild switch`), I ran `npm install` again:

```console
npm info using npm@10.8.2
npm info using node@v20.19.4
npm verbose title npm install
...
npm info run better-sqlite3@9.6.0 install { code: 0, signal: null }
npm info run electron@28.3.3 postinstall node_modules/electron node install.js
npm info run electron@28.3.3 postinstall { code: 0, signal: null }

> taskmgr@1.0.0 postinstall
> electron-builder install-app-deps

  • electron-builder  version=24.13.3
  • loaded configuration  file=package.json ("build" field)
  • rebuilding native dependencies  dependencies=bcrypt@5.1.1, better-sqlite3@9.6.0 platform=linux arch=x64
  • install prebuilt binary  name=better-sqlite3 version=9.6.0 platform=linux arch=x64 napi=

changed 120 packages, and audited 533 packages in 19s
```

Nice! Now it correctly finds a suitable prebuilt version of `better-sqlite3`. Thanks prebuild-install!

Let's start the app by running `npm start`:

```console
$ npm start

> taskmgr@1.0.0 start
> electron .

/home/huzaifa/dev/taskmgr/node_modules/electron/dist/electron: error while loading shared libraries: libgobject-2.0.so.0: cannot open shared object file: No such file or directory
```

Ohoh. Another Nix battle. Looks like `electron` relies on a number of shared libraries that are missing from my system. Let's check which libraries are needed:

```console
$ ldd node_modules/electron/dist/electron
        linux-vdso.so.1 (0x00007fab3e510000)
        libffmpeg.so => /home/huzaifa/dev/taskmgr/node_modules/electron/dist/libffmpeg.so (0x00007fab33400000)
        libdl.so.2 => /nix/store/g8zyryr9cr6540xsyg4avqkwgxpnwj2a-glibc-2.40-66/lib/libdl.so.2 (0x00007fab3e505000)
        libpthread.so.0 => /nix/store/g8zyryr9cr6540xsyg4avqkwgxpnwj2a-glibc-2.40-66/lib/libpthread.so.0 (0x00007fab3e500000)
        libgobject-2.0.so.0 => not found
        libglib-2.0.so.0 => not found
        libgio-2.0.so.0 => not found
        libnss3.so => not found
        libnssutil3.so => not found
        libsmime3.so => not found
        libnspr4.so => not found
        libdbus-1.so.3 => not found
        libatk-1.0.so.0 => not found
        libatk-bridge-2.0.so.0 => not found
        libcups.so.2 => not found
        libdrm.so.2 => not found
        libgtk-3.so.0 => not found
        libpango-1.0.so.0 => not found
        libcairo.so.2 => not found
        libX11.so.6 => not found
        libXcomposite.so.1 => not found
        libXdamage.so.1 => not found
        libXext.so.6 => not found
        libXfixes.so.3 => not found
        libXrandr.so.2 => not found
        libgbm.so.1 => not found
        libexpat.so.1 => not found
        libxcb.so.1 => not found
        libxkbcommon.so.0 => not found
        libasound.so.2 => not found
        libatspi.so.0 => not found
        libm.so.6 => /nix/store/g8zyryr9cr6540xsyg4avqkwgxpnwj2a-glibc-2.40-66/lib/libm.so.6 (0x00007fab33317000)
        libgcc_s.so.1 => /nix/store/16hvpw4b3r05girazh4rnwbw0jgjkb4l-xgcc-14.3.0-libgcc/lib/libgcc_s.so.1 (0x00007fab3e4c6000)
        libc.so.6 => /nix/store/g8zyryr9cr6540xsyg4avqkwgxpnwj2a-glibc-2.40-66/lib/libc.so.6 (0x00007fab33000000)
        /lib64/ld-linux-x86-64.so.2 => /nix/store/g8zyryr9cr6540xsyg4avqkwgxpnwj2a-glibc-2.40-66/lib64/ld-linux-x86-64.so.2 (0x00007fab3e512000)
```

Too many. Now, in a typical distro, you'd have the FHS consisting of `/usr` with subdirectories `bin`, `lib`, `include`, `share`, etc. In Nix, there is no typical FHS. Instead packages are stored in `/nix/store` so that there can't be any unintended dependency on a package that is not explicitly installed by the application. The way you'd add dependencies in Nix is by either creating a derivation with a `.nix` file that specifies the inputs/outputs that the derivation requires/builds. Another way is to create a shell with the dependencies included explicitly. 

`nix-shell -p [packages]` will create a new shell with those packages included in the environment. But there's a problem. `electron`, which is downloaded through `npm` is not patched, and requires the dependent libraries be in FHS-compatible directories, not `/nix/store`, as we saw above. If we had installed `electron` through nixpkgs, then there would be no problem as all the dependencies would be preinstalled beforehand by Nix and `electron` would correctly look for them in `/nix/store`. 

A crude and hardcoded way to solve this problem would be to add the path to the different libraries in `/nix/store` to `LD_LIBRARY_PATH`. But each library is in a separate directory inside `/nix/store` and the path changes version to version. It will be just like fighting with Nix just to make it work.

After doing what I know best (googling) I found a good enough solution for our problem. What we need to do, is to create a sandbox FHS-compliant shell to run `electron` in, where it can find those libraries in `/usr/lib`, but in reality they would be symlinked to `/nix/store`. We need to use `pkgs.buildFHSEnv` for this:

```nix(shell.nix)
{ pkgs ? import <nixpkgs> {} }:

(pkgs.buildFHSEnv {
  name = "electron-env";
  targetPkgs = pkgs: (with pkgs;
    [
      nodejs python3 libcxx systemd libpulseaudio libdrm mesa stdenv.cc.cc
      alsa-lib atk at-spi2-atk at-spi2-core cairo cups dbus expat fontconfig
      freetype gdk-pixbuf glib gtk3 libnotify libuuid nspr nss pango systemd
      libappindicator-gtk3 libdbusmenu libgbm libxkbcommon zlib
    ]
  ) ++ (with pkgs.xorg;
    [
      libXScrnSaver libXrender libXcursor libXdamage libXext libXfixes libXi
      libXrandr libX11 libXcomposite libxshmfence libXtst libxcb
    ]
  );
}).env
```

Now running `nix-shell` in the directory of shell.nix will create a new shell with the FHS environment with the libraries specified residing in `/usr/lib`. Running `npm start` now works!
