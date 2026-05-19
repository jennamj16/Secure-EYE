<?php
/**
 * Custom Secure Directory Indexer
 */
$base_dir = "/var/lib/motioneye/Camera1/"; // Physical path to footage
$web_path = "/recordings/"; // URL path
$current_sub = isset($_GET['dir']) ? $_GET['dir'] : '';
$scan_path = $base_dir . $current_sub;

// Security: Prevent directory traversal
if (strpos(realpath($scan_path), realpath($base_dir)) !== 0) {
    die("Access Denied.");
}

$items = scandir($scan_path);
?>

<!DOCTYPE html>
<html>
<head>
    <title>Archive: <?php echo $current_sub ?: 'Root'; ?></title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #0a0a0a; color: #eee; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: #111; border-radius: 12px; border: 1px solid #222; overflow: hidden; }
        .header { padding: 20px; border-bottom: 1px solid #222; background: #161616; display: flex; align-items: center; }
        .item-row { 
            display: flex; justify-content: space-between; align-items: center; 
            padding: 15px 20px; border-bottom: 1px solid #1f1f1f; transition: background 0.2s;
        }
        .item-row:hover { background: #1a1a1a; }
        .item-link { color: #eee; text-decoration: none; display: flex; align-items: center; flex-grow: 1; }
        .icon { margin-right: 15px; font-size: 20px; }
        
        /* Three Dot Menu */
        .menu-container { position: relative; }
        .dot-btn { 
            background: none; border: none; color: #666; cursor: pointer; 
            font-size: 22px; padding: 5px 10px; border-radius: 50%;
        }
        .dot-btn:hover { color: #fff; background: #333; }
        .dropdown { 
            display: none; position: absolute; right: 0; top: 35px; 
            background: #222; border: 1px solid #444; border-radius: 8px; 
            box-shadow: 0 10px 25px rgba(0,0,0,0.5); z-index: 100; min-width: 180px;
        }
        .dropdown a { 
            display: block; padding: 12px 16px; color: #ddd; text-decoration: none; font-size: 14px; 
        }
        .dropdown a:hover { background: #00bcd4; color: #000; }
        .back-btn { color: #888; text-decoration: none; margin-bottom: 15px; display: inline-block; }
    </style>
</head>
<body>

    <div class="container">
        <div class="header">
            <h2>Index of /recordings/<?php echo htmlspecialchars($current_sub); ?></h2>
        </div>

        <?php if ($current_sub): ?>
            <div class="item-row">
                <a class="item-link" href="?dir=<?php echo urlencode(dirname($current_sub)); ?>">
                    <span class="icon">📁</span> .. (Parent Directory)
                </a>
            </div>
        <?php endif; ?>

        <?php foreach ($items as $item): 
            if ($item == "." || $item == "..") continue;
            $full_item_path = $scan_path . '/' . $item;
            $is_dir = is_dir($full_item_path);
            $rel_path = $current_sub ? $current_sub . '/' . $item : $item;
        ?>
            <div class="item-row">
                <?php if ($is_dir): ?>
                    <a class="item-link" href="?dir=<?php echo urlencode($rel_path); ?>">
                        <span class="icon">📁</span> <?php echo $item; ?>/
                    </a>
                <?php else: 
                    // Only show menu for .mp4 or .enc files
                    $ext = pathinfo($item, PATHINFO_EXTENSION);
                ?>
                    <div class="item-link">
                        <span class="icon">📄</span> <?php echo $item; ?>
                    </div>
                    
                    <div class="menu-container">
                        <button class="dot-btn" onclick="toggleMenu('<?php echo md5($item); ?>')">⋮</button>
                        <div id="<?php echo md5($item); ?>" class="dropdown">
                            <a href="/unlock.php?file=<?php echo urlencode($rel_path); ?>">▶ Play & Decrypt</a>
                            <a href="/verify.php?preselect=<?php echo urlencode($item); ?>">🛡 Verify Authenticity</a>
                            <a href="#" style="color:#f44336;" onclick="confirmDelete('<?php echo $item; ?>')">🗑 Delete File</a>
                        </div>
                    </div>
                <?php endif; ?>
            </div>
        <?php endforeach; ?>
    </div>

    <script>
        function toggleMenu(id) {
            document.querySelectorAll('.dropdown').forEach(d => {
                if(d.id !== id) d.style.display = 'none';
            });
            const m = document.getElementById(id);
            m.style.display = (m.style.display === 'block') ? 'none' : 'block';
        }

        window.onclick = function(e) {
            if (!e.target.matches('.dot-btn')) {
                document.querySelectorAll('.dropdown').forEach(d => d.style.display = 'none');
            }
        }

        function confirmDelete(name) {
            if(confirm("Permanently delete " + name + "?")) {
                // You can link this to your cleanspace.php
                alert("Integration with cleanspace.php triggered for: " + name);
            }
        }
    </script>
</body>
</html>
