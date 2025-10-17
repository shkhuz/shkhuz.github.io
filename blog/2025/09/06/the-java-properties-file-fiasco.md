# The Java .properties file fiasco

My task for today was to integrate a Git library into my blog app. I had a skeleton hand-rolled Java Android app with no build system -- only a Makefile. 

Searching the web, I came across _JGit_. It was written in Java and was near the top of Google Search. So I went with it. I asked ChatGPT what else I would need to add it as a dependency. It recommended `slf4j`, whatever that means. I was new to the Java ecosystem, so I just did what it told me to (I didn't want to waste any more time on a project only I'd use).

After downloading the JARs, I updated the Makefile so that `d8` includes them in the compilation step. When I ran the app, it gave me an error:

```console
Error: Translation bundle could not be found for [org.eclipse.jgit.internal.JGitText, en_US]
```

I ChatGPTed the error and found it was due to a missing `JGitText.properties` file. I didn't have a whole lot of experience working with JARs, so I extracted the `.jar` archive and searched for the file. 

Surprisingly, the `JGitText.properties` file existed and was at the right location in the JAR, so I asked my AI guru what the problem could be. It said that for the particular locale, a `JGitText_en_US.properties` file needs to exist, but also said that if the file is not found, `JGitText.properties` should be the fallback and will be used instead. 

I rechecked that the fallback file existed, and ran the app several times, copying the file with `_en_US` and `_en` suffixes between the runs. But it still didn't work. Neither the US English locale file nor the fallback file was being loaded by JGit at runtime.

I suspected that `d8` has something to do with it; maybe it's just converting the `.class` files in the JAR into `classes.dex`, and ignoring the `.properties` files in the JAR altogether. I once again asked my AI guru about it, and it asserted that indeed `d8` skips files not ending with `.class`, even if you specify the whole `.jar` file to `d8`.

Now here's where the AI ran into a frenzy. It told me to add the `.properties` files to the `.jar` _before_ the dex step. But hold on: the JAR file already has the `.properties` files in it! I updated the `.jar` to have multiple copies of `JGitText.properties` with different suffixes for locales. Even if working with a single `JGitText.properties` file, it should already be present in the JAR, but the AI thinks it's not. Even after telling that the files exist in the JAR, it still prints the previous unhelpful output.

At the end, I got to work. The fix was much simpler: I copied the `.properties` files in the JAR _with the parent directories_, to the root of my project, and put in under a folder called `properties`. Just appending the newly-created `properties` directory path to `aapt` made it work. Unzipping the APK, I could now see the `org/eclipse/jgit/internal` directory with the `.properties` files inside. 
