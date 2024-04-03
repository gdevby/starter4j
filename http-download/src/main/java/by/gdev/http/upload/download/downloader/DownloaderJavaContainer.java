package by.gdev.http.upload.download.downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import by.gdev.util.DesktopUtil;
import by.gdev.util.model.download.JvmRepo;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloaderJavaContainer extends DownloaderContainer {

	private FileMapperService fileMapperService;
	private String workDir;
	private String jreConfig;
	private Path runnableJreDir;

	public static String JRE_DEFAULT = "jre_default";
	public static String JRE_CONFIG = "jreConfig.json";

	public DownloaderJavaContainer(FileMapperService fileMapperService, String workDir, String jreConfig) {
		this.fileMapperService = fileMapperService;
		this.workDir = workDir;
		this.jreConfig = jreConfig;
	}

	public Path getJreDir() {
		return runnableJreDir;
	}

	@Override
	public void filterNotExistResoursesAndSetRepo(Repo repo, String workDirectory)
			throws NoSuchAlgorithmException, IOException {
		JvmRepo jvm = new JvmRepo();
		jvm.setJreDirectoryName(((JvmRepo) repo).getJreDirectoryName());
		this.repo = jvm;

		this.repo.setRepositories(repo.getRepositories());
		this.repo.setResources(Lists.newArrayList());
		for (Metadata meta : repo.getResources()) {
			Path jrePath = Paths.get(workDirectory, JRE_DEFAULT, ((JvmRepo) repo).getJreDirectoryName());
			runnableJreDir = jrePath;
			if (Files.exists(jrePath)) {
				validateJre((JvmRepo) repo);
			} else {
				this.repo.getResources().add(meta);
			}
		}
	}

	private void validateJre(JvmRepo repo) throws IOException, NoSuchAlgorithmException {
		boolean notExistJre = true;
		if (Files.exists(Paths.get(workDir, JRE_DEFAULT, repo.getJreDirectoryName(), JRE_CONFIG))) {
			notExistJre = false;
		}
		List<Metadata> configMetadata = new ArrayList<Metadata>();
		if (!notExistJre) {
			Repo repo1 = fileMapperService
					.read(Paths.get(JRE_DEFAULT, repo.getJreDirectoryName(), jreConfig).toString(), Repo.class);
			if (Objects.nonNull(repo1)) {
				configMetadata = repo1.getResources();
				List<Metadata> localeJreMetadata = DesktopUtil.generateMetadataForJre(workDir,
						Paths.get(JRE_DEFAULT, repo.getJreDirectoryName()).toString());
				configMetadata.removeAll(localeJreMetadata);
			} else {
				notExistJre = true;
			}
		}

		if (notExistJre || configMetadata.size() != 0) {
			Metadata m = repo.getResources().get(0);
			removeJava(Paths.get(workDir, m.getPath()));
			this.repo = repo;
			log.warn("problem with jre");
		}
	}

	private void removeJava(Path path) throws IOException {
		FileUtils.deleteDirectory(Paths.get(workDir, JRE_DEFAULT).toFile());
		FileUtils.deleteQuietly(path.toAbsolutePath().toFile());
	}

	@Override
	public void containerAllSize(Repo repo) {
		containerSize = repo.getResources().stream().map(Metadata::getSize).reduce(Long::sum).orElse(0L);
	}

	@Override
	public void downloadSize(Repo repo, String workDirectory) {
		readyDownloadSize = repo.getResources().stream().map(e -> {
			return Paths.get(workDirectory, e.getPath()).toFile().length();
		}).reduce(Long::sum).orElse(0L);
	}
}