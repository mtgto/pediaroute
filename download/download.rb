# bundle exec ruby download.rb 20140208

require 'nokogiri'
require 'open-uri'
require 'thor'

class Download < Thor
  desc 'check USER DB', 'check new data exists'
  def check(user, db)
    latest_version = get_latest_version
    current_version = get_current_version(user, db)
    if latest_version == current_version
      abort "Already up to date."
    else
      puts latest_version
      latest_version
    end
  end

  desc 'download USER DB', 'download Update wikipedia data and write to mariadb.'
  def download(user, db)
    latest_version = get_latest_version
    current_version = get_current_version(user, db)
    if latest_version == current_version
      abort "すでに最新なのでスキップします"
      exit
    end
    exec('wget --quiet -nc http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-page.sql.gz')
    exec('wget --quiet -nc http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-pagelinks.sql.gz')
    exec("echo 'DROP TABLE IF EXISTS page; DROP TABLE IF EXISTS pagelinks;' | mysql -u #{user} #{db}")
    exec("gzcat jawiki-latest-page.sql.gz | time mysql -u #{user} #{db}")
    exec("gzcat jawiki-latest-pagelinks.sql.gz | time mysql -u #{user} #{db}")
    exec('rm jawiki-latest-page.sql.gz')
    exec('rm jawiki-latest-pagelinks.sql.gz')
    if current_version.empty?
      exec("echo 'INSERT INTO version VALUES(#{latest_version});' | mysql -u #{user} #{db}") 
    else
      exec("echo 'UPDATE version SET name = #{latest_version};' | mysql -u #{user} #{db}")
    end
  end

  private
  def get_current_version(user, db)
    exec("echo 'CREATE TABLE IF NOT EXISTS version(name char(8));' | mysql -u #{user} #{db}")
    version = `echo 'SELECT name FROM version' | mysql -u #{user} #{db} | tail -n 1`.strip
    version[0, 8]
  end

  def get_latest_version
    doc = Nokogiri::HTML(open('http://dumps.wikimedia.org/jawiki/'))
    doc.css('a')[-2].text.sub(/[^\d]/, '')
  end

  def exec(cmd)
    STDERR.puts "executing \"#{cmd}\""
    exit(1) unless system(cmd)
  end
end

Download.start(ARGV)

