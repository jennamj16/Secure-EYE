<?php
/**
 * UNLOCK.PHP - Final Optimized Version
 */
$registry_path = "/home/pi-cam/video_registry.json";
$base_dir      = "/var/lib/motioneye/Camera1/";
$public_key    = "/var/local/public.pem";
$gpg_home      = "/var/www/.gnupg"; 
$staging_dir   = "/var/www/html/staging/";
$passphrase    = "MeAdmin@43#@43#";

if (!isset($_GET['file'])) die("No file specified.");
$requested_file = basename(urldecode($_GET['file']));

// STAGE 1: THE LOADING SCREEN
if (!isset($_GET['view'])) {
    ?>
    <!DOCTYPE html>
    <html>
    <head>
        <title>Processing...</title>
        <style>
            body { background: #000; color: #fff; font-family: sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
            .loader { text-align: center; }
            .progress-bar { width: 300px; height: 10px; background: #222; border-radius: 5px; margin-top: 20px; overflow: hidden; position: relative; }
            .progress-fill { width: 100%; height: 100%; background: #00bcd4; animation: slide 2s infinite; }
            @keyframes slide { from { transform: translateX(-100%); } to { transform: translateX(100%); } }
        </style>
    </head>
    <body>
        <div class="loader">
            <h2>Decrypting Video...</h2>
            <div class="progress-bar"><div class="progress-fill"></div></div>
            <p>Verifying RSA Digital Signature...</p>
        </div>
        <script>setTimeout(() => { window.location.href = window.location.href + "&view=1"; }, 100);</script>
    </body>
    </html>
    <?php
    exit;
}

// STAGE 2: DECRYPTION AND PLAYER
$registry = json_decode(file_get_contents($registry_path), true);
$found_key = null;
foreach ($registry as $path => $data) { if (basename($path) === $requested_file) { $found_key = $path; break; } }
if (!$found_key) die("Registry mapping failed.");
$entry = $registry[$found_key];

$original_name = basename($found_key);
$encrypted_source = $base_dir . ltrim($found_key, '/') . ".enc";
$decrypted_output = $staging_dir . $original_name;

putenv("GNUPGHOME=$gpg_home");
$decrypt_cmd = "gpg --batch --yes --pinentry-mode loopback --passphrase " . escapeshellarg($passphrase) . " --output " . escapeshellarg($decrypted_output) . " --decrypt " . escapeshellarg($encrypted_source) . " 2>&1";
exec($decrypt_cmd, $output, $return_var);

// Cryptographic Verification
$current_hash = hash_file('sha256', $decrypted_output);
$hash_match   = (trim($current_hash) === trim($entry['hash']));

// Signature check (Must be done on the decrypted file)
$sig_tmp = tempnam(sys_get_temp_dir(), 'sig');
file_put_contents($sig_tmp, base64_decode($entry['sig']));
$verify_cmd = "openssl dgst -sha256 -verify " . escapeshellarg($public_key) . " -signature " . escapeshellarg($sig_tmp) . " " . escapeshellarg($decrypted_output) . " 2>&1";
$sig_status = shell_exec($verify_cmd);
$sig_match  = (strpos($sig_status, 'Verified OK') !== false);
unlink($sig_tmp);
?>

<!DOCTYPE html>
<html>
<head>
    <title>Secure Playback</title>
    <style>
        body { font-family: sans-serif; background: #0a0a0a; color: #eee; padding: 20px; text-align: center; }
        .box { max-width: 850px; margin: auto; background: #161616; padding: 25px; border-radius: 12px; border: 1px solid #333; }
        .nav { display: flex; justify-content: space-between; margin-bottom: 20px; }
        .btn { padding: 12px 20px; border-radius: 6px; border: none; font-weight: bold; cursor: pointer; color: white; text-decoration: none; }
        .btn-back { background: #444; }
        .btn-del { background: #b71c1c; }
        video { width: 100%; border-radius: 8px; background: #000; }
        .report { margin-top: 20px; text-align: left; background: #000; padding: 15px; border-radius: 8px; font-family: monospace; }
        .row { display: flex; justify-content: space-between; margin-bottom: 10px; padding: 10px; border-radius: 4px; }
        .green { border-left: 5px solid #4caf50; color: #4caf50; background: rgba(76, 175, 80, 0.05); }
        .red { border-left: 5px solid #f44336; color: #f44336; background: rgba(244, 67, 54, 0.05); }
    </style>
</head>
<body>
    <div class="box">
        <div class="nav">
            <a href="javascript:history.back()" class="btn btn-back">⬅ Back</a>
        </div>

        <video controls autoplay><source src="/staging/<?php echo $original_name; ?>?v=<?php echo time(); ?>" type="video/mp4"></video>

        <div class="report">
            <div class="row <?php echo $hash_match ? 'green' : 'red'; ?>">
                <span>SHA256 Integrity:</span>
                <span><?php echo $hash_match ? 'MATCHED' : 'MISMATCHED'; ?></span>
            </div>
            <div class="row <?php echo $sig_match ? 'green' : 'red'; ?>">
                <span>RSA Signature:</span>
                <span><?php echo $sig_match ? 'VERIFIED OK' : 'FAILURE'; ?></span>
            </div>
            <div style="font-size: 0.8em; color: #555; padding: 10px; word-break: break-all;">
                Hash: <?php echo $current_hash; ?><br>
                Sig: <?php echo trim($sig_status); ?>
            </div>
        </div>
    </div>

<script>
</body>
</html>
