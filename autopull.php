<?php
/**
  * This script is for easily deploying updates to Github repos to your local server. It will automatically git clone or
  * git pull in your repo directory every time an update is pushed to your $BRANCH (configured below).
  *
  * INSTRUCTIONS:
  * 1. Edit the variables below
  * 2. Upload this script to your server somewhere it can be publicly accessed
  * 3. Make sure the apache user owns this script (e.g., sudo chown www-data:www-data webhook.php)
  * 4. (optional) If the repo already exists on the server, make sure the same apache user from step 3 also owns that
  *    directory (i.e., sudo chown -R www-data:www-data)
  * 5. Go into your Github Repo > Settings > Service Hooks > WebHook URLs and add the public URL
  *    (e.g., http://example.com/webhook.php)
  *
**/

// Set Variables
$LOCAL_ROOT         = ".";
$LOCAL_REPO_NAME    = ".";
$LOCAL_REPO         = "{$LOCAL_ROOT}/{$LOCAL_REPO_NAME}";
$REMOTE_REPO        = "http://github.com/jschnepple/arena-api";
$BRANCH             = "master";
if ( $_SERVER['REQUEST_METHOD'] == "POST") {
  // Only respond to POST requests from Github

    // If there is already a repo, just run a git pull to grab the latest changes
    shell_exec("git pull");
    //shell_exec("cd {$LOCAL_REPO} && git pull");
    die("done " . mktime());
}

?>