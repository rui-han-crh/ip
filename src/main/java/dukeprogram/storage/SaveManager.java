package dukeprogram.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;
import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import exceptions.KeyNotFoundException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * SaveManager manages all the save functionalities
 */
public class SaveManager {

    private static Storage dataInMemory;

    /**
     * A description of all images that can be loaded
     */
    public enum ImageType {
        DukeImage("Duke.png", new Image(Objects.requireNonNull(
                SaveManager.class.getResourceAsStream("/images/DaDuke.png")))),
        UserImage("User.png", new Image(Objects.requireNonNull(
                SaveManager.class.getResourceAsStream("/images/DaUser.png"))));

        public final String label;
        public final Image defaultImage;

        ImageType(String label, Image defaultImage) {
            this.label = label;
            this.defaultImage = defaultImage;
        }
    }

    private static final String DATA_FOLDER = System.getProperty("user.home");
    private static final Path PATH = java.nio.file.Paths.get(
            DATA_FOLDER,
            "CS2103T",
            "DukeData"
    );

    private static final Path PROFILE_PICTURES = PATH.resolve("ProfilePictures");

    private static ObjectMapper objectMapper;

    /**
     * Saves to the storage object
     * @param header the name of the variable to associate the saved object with
     * @param obj the saved object
     */
    public static void save(String header, Serializable obj) {
        dataInMemory.put(header, obj);
    }

    /**
     * Loads to the storage object
     * @param header the name of the variable to associate the saved object with
     * @return a Serializable object if possible
     * @throws KeyNotFoundException if no such header name exists
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T load(String header) throws KeyNotFoundException {
        assert dataInMemory != null;
        return (T) dataInMemory.get(header);
    }

    /**
     * Retrieves a profile picture with the name <code>header</code>
     * @param imageType the supported Image type to load
     * @return an image with the given name
     */
    public static Image loadProfilePicture(ImageType imageType) {
        File profilePicturesDirectory = getProfilePicturesDirectory();
        File imageFile = new File(profilePicturesDirectory, imageType.label);

        try {
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            Image image = new Image(fileInputStream);
            fileInputStream.close();
            return image;
        } catch (FileNotFoundException e) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(imageType.defaultImage, null),
                        "png", imageFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return imageType.defaultImage;
        } catch (NullPointerException | IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void createObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Serializable.class)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    /**
     * Serializes a file to disk under the path
     * @param fileName the file name to serialize to under the folder path
     */
    public static void serialize(String fileName) throws IOException {
        if (objectMapper == null) {
            createObjectMapper();
        }

        File saveFile = new File(PATH.toString(), fileName);

        creatingDirectoriesUpTo(saveFile.getParentFile());

        try {
            objectMapper.writeValue(saveFile, dataInMemory);
            System.out.println("Serialization successful, saved at " + saveFile.getPath());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deserializes a file from disk under the path
     * @param fileName the file name to deserialize from
     * @return whether deserialization was successful
     */
    public static boolean deserialize(String fileName) {
        if (objectMapper == null) {
            createObjectMapper();
        }

        File file = new File(PATH.toString(), fileName);
        try {
            dataInMemory = objectMapper.readValue(file, Storage.class);
            assert dataInMemory != null;
            System.out.println("Saved data successfully retrieved");
            return true;
        } catch (IOException e) {
            if (file.exists()) {
                System.out.println("Deserialization error, likely corrupt file");
                System.out.println(e.getMessage());
            }
            dataInMemory = new Storage();
            return false;
        }
    }

    public static File getProfilePicturesDirectory() {
        File dir = new File(PROFILE_PICTURES.toString());
        creatingDirectoriesUpTo(dir);
        return dir;
    }

    /**
     * Creates directories to this folder, if they do not exist, including this folder.
     * Nothing is performed if the directories already exist
     * @param folder the end folder to create directories to
     */
    private static void creatingDirectoriesUpTo(File folder) {
        try {
            if (folder.mkdirs()) {
                System.out.println("Created subdirectories at " + folder.getPath());
            }
        } catch (SecurityException e) {
            System.out.println("Read or writing permissions denied at " + folder.getPath());
        }
    }
}
