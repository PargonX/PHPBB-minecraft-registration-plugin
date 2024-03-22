package network.vonix.registrationplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegistrationPlugin extends JavaPlugin {

    private String forumUrl;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        loadConfig();

        getLogger().info("RegistrationPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RegistrationPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("register")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length != 2) {
                sender.sendMessage("Usage: /register <email> <password>");
                return true;
            }

            String email = args[0];
            String password = args[1];

            // Construct the POST data
            String postData = "username=" + player.getName() + "&email=" + email + "&password=" + password;

            try {
                // Create connection to the registration endpoint
                URL url = new URL(forumUrl + "/regiapi.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Write POST data to the connection
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                    os.write(postDataBytes);
                }

                // Check response code
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    sender.sendMessage("Registration successful!");
                } else {
                    sender.sendMessage("Registration failed. HTTP error code: " + responseCode);
                }

                conn.disconnect();
            } catch (Exception e) {
                sender.sendMessage("An error occurred while registering: " + e.getMessage());
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        this.forumUrl = config.getString("forum_url");
        if (this.forumUrl == null || this.forumUrl.isEmpty()) {
            getLogger().warning("forum_url is not specified in config.yml. Please specify the forum URL.");
        } else {
            // Add https:// prefix if not present
            if (!forumUrl.startsWith("https://")) {
                forumUrl = "https://" + forumUrl;
            }
        }
    }
}
