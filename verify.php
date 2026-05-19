<?php  
/**
 * VERIFY.PHP - Search by Hash
 */
$registry_path = "/home/pi-cam/video_registry.json";
$report = null;
$search_hash = isset($_POST['hash_input']) ? trim($_POST['hash_input']) : null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $search_hash) {
    if (file_exists($registry_path)) {
        $registry = json_decode(file_get_contents($registry_path), true);
        $found_entry = null;

        // Search registry for the pasted hash
        foreach ($registry as $path => $data) {
            if (trim($data['hash']) === $search_hash) {
                $found_entry = $data;
                $found_entry['filename'] = basename($path);
                break;
            }
        }

        if ($found_entry) {
            $report = [
                'status' => 'SUCCESS',
                'msg' => "✅ Match Found!",
                'file' => $found_entry['filename'],
                'date' => date("Y-m-d H:i:s", filemtime($registry_path)) 
            ];
        } else {
            $report = [
                'status' => 'FAIL',
                'msg' => "❌ No match found in registry."
            ];
        }
    } else {
        $report = ['status' => 'FAIL', 'msg' => "Registry file missing."];
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Hash Verifier</title>
    <style>
        body { font-family: sans-serif; background: #0a0a0a; color: #eee; text-align: center; padding: 40px; }
        .container { max-width: 700px; margin: auto; background: #161616; padding: 30px; border-radius: 12px; border: 1px solid #333; }
        input[type="text"] { 
            width: 100%; padding: 12px; background: #000; border: 1px solid #444; 
            color: #00bcd4; border-radius: 4px; font-family: monospace; box-sizing: border-box;
        }
        .btn { background: #00bcd4; color: #000; padding: 12px 25px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; margin-top: 15px; }
        .result { margin-top: 25px; padding: 20px; border-radius: 8px; text-align: left; }
        .SUCCESS { border: 1px solid #4caf50; background: rgba(76, 175, 80, 0.1); color: #4caf50; }
        .FAIL { border: 1px solid #f44336; background: rgba(244, 67, 54, 0.1); color: #f44336; }
    </style>
</head>
<body>

    <div class="container">
        <h2>🛡 Metadata Hash Search</h2>
        <p>Paste the SHA256 hash of the video to verify if it exists in the system records.</p>

        <form method="POST" action="/verify/">
            <input type="text" name="hash_input" placeholder="Paste SHA256 Hash here..." value="<?php echo htmlspecialchars($search_hash); ?>" required>
            <button type="submit" class="btn">Search Registry</button>
        </form>

        <?php if ($report): ?>
            <div class="result <?php echo $report['status']; ?>">
                <strong><?php echo $report['msg']; ?></strong>
                <?php if ($report['status'] === 'SUCCESS'): ?>
                    <p><strong>Original File:</strong> <?php echo $report['file']; ?></p>
                    <p style="font-size: 0.8em; opacity: 0.7;">This hash is cryptographically linked to a verified recording on this server.</p>
                <?php endif; ?>
            </div>
        <?php endif; ?>

        <br>
        <a href="/recordings/" style="color: #888; text-decoration: none;">⬅ Back to Archives</a>
    </div>

</body>
</html>
